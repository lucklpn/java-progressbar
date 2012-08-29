package hu.ssh.progressbar.console;

import hu.ssh.progressbar.AbstractProgressBar;
import hu.ssh.progressbar.Progress;
import hu.ssh.progressbar.console.replacers.BarReplacer;
import hu.ssh.progressbar.console.replacers.ElapsedTimeReplacer;
import hu.ssh.progressbar.console.replacers.PercentageReplacer;
import hu.ssh.progressbar.console.replacers.RemainingTimeReplacer;
import hu.ssh.progressbar.console.replacers.Replacer;
import hu.ssh.progressbar.console.replacers.TotalTimeReplacer;

import java.io.PrintStream;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

/**
 * A console progress bar.
 * <p>
 * It can display a human readable progress bar with the specified format on the selected
 * output.
 * <p>
 * Here is a basic usage:
 *
 * <pre>
 * ProgressBar progressBar = ConsoleProgressBar.on(System.out);
 * progressBar.tick(20);
 * progressBar.finish();
 * </pre>
 *
 * It can be configured with custom format:
 *
 * <pre>
 * ProgressBar progressBar = ConsoleProgressBar.on(System.out)
 * 		.withFormat(&quot;[:bar]&quot;)
 * 		.withProgressBarWidth(70);
 * </pre>
 *
 * Or can be set to use custom number of steps instead of the default <code>100</code>:
 *
 * <pre>
 * ProgressBar progressBar = ConsoleProgressBar.on(System.out)
 * 		.withTotalSteps(25);
 * </pre>
 *
 * @author KARASZI István (github@spam.raszi.hu)
 */
public final class ConsoleProgressBar extends AbstractProgressBar {
	public static final char LINE_FEED = '\n';
	public static final char CARRIAGE_RETURN = '\r';

	public static final String DEFAULT_FORMAT = "[:bar] :percent% :eta";
	public static final int DEFAULT_PROGRESSBAR_WIDTH = 60;
	public static final long DEFAULT_STEPS = 100;

	private final Set<Replacer> availableReplacers;
	private final PrintStream streamToUse;
	private final String outputFormat;
	private final int progressBarWidth;

	private int previousLength = 0;

	private ConsoleProgressBar(final PrintStream streamToUse, final String progressBarFormat, final int progressBarWidth,
			final long totalSteps)
	{
		super(totalSteps);

		this.progressBarWidth = progressBarWidth;
		this.availableReplacers = getReplacers(progressBarWidth);
		this.streamToUse = streamToUse;
		this.outputFormat = progressBarFormat;
	}

	/**
	 * A console progress bar on the selected PrintStream.
	 *
	 * @param streamToUse
	 *            the PrintStream on the ProgressBar should appear
	 * @return a progress bar with the desired configuration
	 */
	public static ConsoleProgressBar on(final PrintStream streamToUse) {
		Preconditions.checkNotNull(streamToUse);

		return new ConsoleProgressBar(streamToUse, DEFAULT_FORMAT, DEFAULT_PROGRESSBAR_WIDTH, DEFAULT_STEPS);
	}

	/**
	 * Changes the output format of the actual ProgressBar.
	 *
	 * @param outputFormat
	 *            the new output format
	 * @return a progress bar with the desired configuration
	 */
	public ConsoleProgressBar withFormat(final String outputFormat) {
		Preconditions.checkNotNull(outputFormat);

		return new ConsoleProgressBar(streamToUse, outputFormat, progressBarWidth, totalSteps);
	}

	/**
	 * Changes the total steps of the actual ProgressBar.
	 *
	 * @param totalSteps
	 * @return
	 */
	public ConsoleProgressBar withTotalSteps(final int totalSteps) {
		Preconditions.checkArgument(totalSteps != 0);

		return new ConsoleProgressBar(streamToUse, outputFormat, progressBarWidth, totalSteps);
	}

	public ConsoleProgressBar withProgressBarWidth(final int progressBarWidth) {
		Preconditions.checkArgument(progressBarWidth != 0);

		return new ConsoleProgressBar(streamToUse, outputFormat, progressBarWidth, totalSteps);
	}

	private static Set<Replacer> getReplacers(final int progressBarWidth) {
		return ImmutableSet.of(
				new BarReplacer(progressBarWidth),
				new PercentageReplacer(),
				new RemainingTimeReplacer(),
				new ElapsedTimeReplacer(),
				new TotalTimeReplacer()
				);
	}

	@Override
	protected void updateProgressBar(final Progress progress) {
		final String actualBar = getActualProgressBar(progress);

		streamToUse.print(actualBar);
		streamToUse.print(getGarbageCleaning(actualBar.length()));
		streamToUse.print(CARRIAGE_RETURN);
	}

	@Override
	protected void finishProgressBar() {
		streamToUse.print(LINE_FEED);
	}

	private String getActualProgressBar(final Progress progress) {
		String bar = outputFormat;

		for (final Replacer replacer : availableReplacers) {
			final String identifier = replacer.getReplaceIdentifier();

			if (!bar.contains(identifier)) {
				continue;
			}

			bar = bar.replace(identifier, replacer.getReplacementForProgress(progress));
		}

		return bar;
	}

	private String getGarbageCleaning(final int actualLength) {
		if (actualLength >= previousLength) {
			return "";
		}

		final String garbageCleaner = Strings.repeat(" ", previousLength - actualLength);
		previousLength = actualLength;

		return garbageCleaner;
	}
}