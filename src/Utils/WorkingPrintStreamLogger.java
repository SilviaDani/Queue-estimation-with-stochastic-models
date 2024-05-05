package Utils;

import org.oristool.analyzer.log.AnalysisLogger;


import java.io.PrintStream;

public class WorkingPrintStreamLogger implements AnalysisLogger {
    private final PrintStream ps;
    private boolean debug;
    public WorkingPrintStreamLogger(PrintStream ps, boolean debug) {
        this.ps = ps;
        this.debug = debug;
    }

    @Override
    public void log(String s) {
        ps.print(s);
    }

    @Override
    public void debug(String s) {
        if (debug) {
            ps.print(s);
        }
    }
}
