package application.utils;

import application.error.Error;
import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RemoteDebug implements Runnable {
    private String ip;
    private String port;
    private VirtualMachine vm;
    private List<BreakPoint> breakPoints = new ArrayList<BreakPoint>();

    private static Logger log = Logger.getLogger(RemoteDebug.class);

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
            Error.REMOTE_DEBUG_SET_BREAKPOINT.record().create(ex);
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
                                            log.info("Breakpoint at line " + bpReq.location().lineNumber() + ": ");
                                            Value val = stackFrame.getValue(visibleVar);
                                            if (val instanceof StringReference) {
                                                String varNameValue = ((StringReference) val).value();
                                                log.info(visibleVar.name() + " = '" + varNameValue + "'");
                                            } else if (val instanceof ObjectReference) {
                                                ObjectReference reference = ((ObjectReference) val);
                                                ReferenceType thing = reference.referenceType();

                                                Method method = thing.methodsByName("getCode").get(0);

                                                Value value = reference.invokeMethod(stackFrame.thread(), method, new ArrayList<Value>(), 0);
                                                if (value instanceof StringReference) {
                                                    String varNameValue = ((StringReference) value).value();
                                                    log.info("RESULT! -> " + varNameValue);
                                                }
                                                for (Field field : thing.fields()) {
                                                    log.info("    " + field + "->" + reference.getValue(field));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (AbsentInformationException ex) {
                            Error.REMOTE_DEBUG_COMPILE.record().create(ex);
                        } catch (Exception ex) {
                            Error.REMOTE_DEBUG_EVENT.record().create(ex);
                        } finally {
                            evtSet.resume();
                        }
                    }
                }
            }
        } catch (InterruptedException | IllegalConnectorArgumentsException | IOException ex) {
            Error.REMOTE_DEBUG.record().create(ex);
        }
    }
}
