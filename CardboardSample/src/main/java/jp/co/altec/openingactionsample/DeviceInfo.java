package jp.co.altec.openingactionsample;

/**
 * Created by tokue on 2015/11/29.
 */
public class DeviceInfo {
    private String mName = "none";
    private String mIpAddress = "0.0.0";
    private Point mPoint = new Point("-1", "-1", "-1");

    public DeviceInfo(String name, String ipAddress, Point point) {
        mName = name;
        mIpAddress = ipAddress;
        mPoint = point;
    }

    public String getName() { return mName; }

    public Point getPoint() { return mPoint; }

    public void setPoint(Point point) { this.mPoint = point; }

    public String getIpAddress() { return mIpAddress; }

    public void setIpAddress(String ipAddress) { this.mIpAddress = ipAddress; }

    public void setName(String name) { this.mName = name; }

    public String Format() {
        return mName + ":" + mIpAddress + ":" + mPoint.x + ":" +  mPoint.y + ":" +  mPoint.z;
    }

    public static DeviceInfo parse(String in) {
        String[] data = in.split(":");
        return new DeviceInfo(data[0],data[1],new Point(data[2],data[3],data[4]));
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "Name='" + mName + '\'' +
                "IpAddress='" + mIpAddress + '\'' +
                ", Point=" + mPoint +
                '}';
    }
}
