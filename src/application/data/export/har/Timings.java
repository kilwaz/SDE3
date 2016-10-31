package application.data.export.har;

public class Timings extends HarObject {
    private HarObject blocked = new HarObject("blocked", new Integer(-1)); // Defaults to -1
    private HarObject dns = new HarObject("dns", new Integer(-1)); // Defaults to -1
    private HarObject connect = new HarObject("connect", new Integer(-1)); // Defaults to -1
    private HarObject send = new HarObject("send", new Integer(-1)); // Defaults to -1
    private HarObject wait = new HarObject("wait", new Integer(-1)); // Defaults to -1
    private HarObject receive = new HarObject("receive", new Integer(-1)); // Defaults to -1
    private HarObject ssl = new HarObject("ssl", new Integer(-1)); // Defaults to -1

    public Timings() {
        super("timings", HarObject.OBJECT);
    }

    public void setSend(String send) {
        this.send = new HarObject("send", send);
    }

    public void setWait(Integer wait) {
        this.wait = new HarObject("wait", wait);
    }

    public void setReceive(Integer receive) {
        this.receive = new HarObject("receive", receive);
    }

    public void setBlocked(Integer blocked) {
        this.blocked = new HarObject("blocked", blocked);
    }

    public void setDns(String dns) {
        this.dns = new HarObject("dns", dns);
    }

    public void setConnect(String connect) {
        this.connect = new HarObject("connect", connect);
    }

    public void setSsl(String ssl) {
        this.ssl = new HarObject("ssl", ssl);
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        output.writeObject(blocked);
        output.writeObject(dns);
        output.writeObject(connect);
        output.writeObject(send);
        output.writeObject(wait);
        output.writeObject(receive);
        output.writeObject(ssl);
        output.writeObject(getComment());
        output.endObject();
    }
}
