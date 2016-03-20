package be.planetsizebrain.crash.domain;

public enum ConsoleSize {

	AUTO(0, 0),
	SVGA(800, 600),
	XGA(1024, 768),
	WXGA(1280, 720);

	private static final String LABEL_SKELETON = "%s (%s)";
	private static final String RESOLUTION_SKELETON = "%sx%s";

	private final int width;
	private final int height;

	ConsoleSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public static ConsoleSize getByResolution(String resolution) {
		for (ConsoleSize consoleSize : values()) {
			String res = String.format(RESOLUTION_SKELETON, consoleSize.getWidth(), consoleSize.getHeight());
			if (res.equals(resolution)) {
				return consoleSize;
			}
		}

		return AUTO;
	}

	public String getResolution() {
		return String.format(RESOLUTION_SKELETON, width, height);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String getLabel() {
		String resolution = String.format(RESOLUTION_SKELETON, width, height);
		if (this == AUTO) {
			resolution = "max width x max height";
		}

		return String.format(LABEL_SKELETON, name(), resolution);
	}
}