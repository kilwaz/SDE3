package application.utils;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoteDebug implements Runnable {
    private String ip;
    private String port;
    private VirtualMachine vm;
    private List<BreakPoint> breakPoints = new ArrayList<BreakPoint>();

    private class BreakPoint {
        private String className;
        private String variableName;
        private int lineNumber;

        public BreakPoint(String className, String variableName, int lineNumber) {
            this.className = className;
            this.variableName = variableName;
            this.lineNumber = lineNumber;
        }

        public String getClassName() {
            return this.className;
        }

        public String getVariableName() {
            return variableName;
        }

        public int getLineNumber() {
            return this.lineNumber;
        }
    }

    public RemoteDebug(String ip, String port) {
        this.ip = ip;
        this.port = port;
    }

    public void setBreakPoint(String className, String variableName, int lineNumber) {
        breakPoints.add(new BreakPoint(className, variableName, lineNumber));

        try {
            List<ReferenceType> classList = vm.classesByName(className);
            Location breakpointLocation = null;
            for (ReferenceType refType : classList) {
                if (breakpointLocation != null) {
                    break;
                }
                List<Location> locs = refType.allLineLocations();
                for (Location loc : locs) {
                    if (loc.lineNumber() == lineNumber) {
                        breakpointLocation = loc;
                        break;
                    }
                }
            }

            if (breakpointLocation != null) {
                EventRequestManager evtReqMgr = vm.eventRequestManager();
                BreakpointRequest bReq = evtReqMgr.createBreakpointRequest(breakpointLocation);
                bReq.setSuspendPolicy(BreakpointRequest.SUSPEND_ALL);
                bReq.enable();
            }
        } catch (AbsentInformationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            VirtualMachineManager vmm = com.sun.jdi.Bootstrap.virtualMachineManager();
            AttachingConnector attachingConnector = null;
            for (AttachingConnector attachingConnectorIterator : vmm.attachingConnectors()) {
                if ("dt_socket".equalsIgnoreCase(attachingConnectorIterator.transport().name())) {
                    attachingConnector = attachingConnectorIterator;
                }
            }

            if (attachingConnector != null) {
                Map<String, Connector.Argument> prm = attachingConnector.defaultArguments();
                prm.get("port").setValue(port);
                prm.get("hostname").setValue(ip);
                vm = attachingConnector.attach(prm);
                EventQueue evtQueue = vm.eventQueue();
                while (true) {
                    EventSet evtSet = evtQueue.remove(); // Code pauses here..
                    EventIterator evtIter = evtSet.eventIterator();
                    while (evtIter.hasNext()) {
                        try {
                            Event evt = evtIter.next();
                            EventRequest evtReq = evt.request();
                            if (evtReq instanceof BreakpointRequest) {
                                BreakpointRequest bpReq = (BreakpointRequest) evtReq;
                                BreakpointEvent brEvt = (BreakpointEvent) evt;
                                ThreadReference threadRef = brEvt.thread();
                                StackFrame stackFrame = threadRef.frame(0);

                                List<LocalVariable> visVars = stackFrame.visibleVariables();
                                for (LocalVariable visibleVar : visVars) {
                                    for (BreakPoint breakPoint : breakPoints) {
                                        if (breakPoint.getLineNumber() == bpReq.location().lineNumber() && visibleVar.name().equals(breakPoint.getVariableName())) {
                                            System.out.println("Breakpoint at line " + bpReq.location().lineNumber() + ": ");
                                            Value val = stackFrame.getValue(visibleVar);
                                            if (val instanceof StringReference) {
                                                String varNameValue = ((StringReference) val).value();
                                                System.out.println(visibleVar.name() + " = '" + varNameValue + "'");
                                            } else if (val instanceof ObjectReference) {
                                                ObjectReference reference = ((ObjectReference) val);
                                                ReferenceType thing = reference.referenceType();

                                                Method method = thing.methodsByName("getCode").get(0);

                                                Value value = reference.invokeMethod(stackFrame.thread(), method, new ArrayList<Value>(), 0);
                                                if (value instanceof StringReference) {
                                                    String varNameValue = ((StringReference) value).value();
                                                    System.out.println("RESULT! -> " + varNameValue);
                                                }
                                                for (Field field : thing.fields()) {
                                                    System.out.println("    " + field + "->" + reference.getValue(field));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (AbsentInformationException aie) {
                            System.out.println("AbsentInformationException: did you compile your target application with -g option?");
                        } catch (Exception exc) {
                            System.out.println(exc.getClass().getName() + ": " + exc.getMessage());
                        } finally {
                            evtSet.resume();
                        }
                    }
                }
            }
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        } catch (IllegalConnectorArgumentsException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
