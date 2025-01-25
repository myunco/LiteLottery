package ml.mcos.litelottery.util;

public class Version {
    private final int major;
    private final int minor;
    private int patch;

    public Version(String bukkitVersion) {
        // 1.x.x-R0.x-SNAPSHOT
        String[] parts = bukkitVersion.replace('-', '.').split("\\.");
        this.major = Integer.parseInt(parts[0]);
        this.minor = Integer.parseInt(parts[1]);
        try {
            this.patch = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            this.patch = 0;
        }
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    @Override
    public String toString() {
        return "" + major + minor + patch;
    }
}
