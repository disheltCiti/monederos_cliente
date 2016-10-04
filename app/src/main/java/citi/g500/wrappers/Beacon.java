package citi.g500.wrappers;

/**
 * Created by ftorres on 22/09/2016.
 */
public class Beacon implements Comparable {

    private final String uui;
    private final String major;
    private final String minor;
    private final double distance;

    public Beacon(String uui, String major, String minor, double distance) {
        this.uui = uui;
        this.major = major;
        this.minor = minor;
        this.distance = distance;
    }

    @Override
    public int hashCode() {
        return (this.getUui() + ";" + this.getMajor() + ";" + this.getMinor()).hashCode();
    }

    public String getUui() {
        return this.uui;
    }

    public String getMajor() {
        return this.major;
    }

    public String getMinor() {
        return this.minor;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Object object) {
        Beacon otherBeacon = (Beacon) object;
        if (this.hashCode() == otherBeacon.hashCode()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "UUI:" + uui + "\tMajor:" + major + "\tMinor:" + minor + "\tDistance:" + distance;
    }
}
