/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2012  International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CIN/IARC, ervikm@iarc.fr
 */
package canreg.client.analysis;

import canreg.client.analysis.TableBuilderInterface.ChartType;
import canreg.client.analysis.TableBuilderInterface.FileTypes;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author ErvikM
 */
public class RFileBuilder {

    StringBuilder rScript;
    String palette = "Paired";
    NumberFormat numberFormat = NumberFormat.getInstance();

    public RFileBuilder() {
        rScript = new StringBuilder();
        numberFormat.setGroupingUsed(false);
        numberFormat.setMinimumFractionDigits(1);
        numberFormat.setMaximumFractionDigits(1);
    }

    public void appendHeader(String headerScriptPath) {
        String fileNameForR = headerScriptPath.replace("\\", "/");
        rScript.append("source(\"").append(fileNameForR).append("\")\n");
    }

    public void appendFileTypePart(FileTypes fileType, String fileName) {
        String fileNameForR = fileName.replace("\\", "/");
        if (fileType.equals(FileTypes.svg)) {
            rScript.append("svg(\"").append(fileNameForR).append("\")\n");
        } else if (fileType.equals(FileTypes.pdf)) {
            rScript.append("pdf(\"").append(fileNameForR).append("\")\n");
        } else if (fileType.equals(FileTypes.ps)) {
            rScript.append("ps(\"").append(fileNameForR).append("\")\n");
        } else {
            rScript.append("png(\"").append(fileNameForR).append("\")\n");
        }
    }

    public void appendData(Collection<CancerCasesCount> counts, Double restCount, boolean includeOther) {
        StringBuilder countsLine = new StringBuilder();
        StringBuilder labelsLine = new StringBuilder();
        countsLine.append("counts <- c(");
        labelsLine.append("labels <- factor(c(");
        Iterator<CancerCasesCount> iter = counts.iterator();
        CancerCasesCount count;
        while (iter.hasNext()) {
            count = iter.next();
            if (count.getCount() < 1) {
                numberFormat.setMaximumFractionDigits(2);
                numberFormat.setMinimumFractionDigits(2);
            } else {
                numberFormat.setMaximumFractionDigits(1);
                numberFormat.setMinimumFractionDigits(1);
            }
            countsLine.append(numberFormat.format(count.getCount()));
            labelsLine.append("\"").append(count.getLabel()).append("\"");
            if (iter.hasNext()) {
                countsLine.append(", ");
                labelsLine.append(", ");
            }
        }

        if (includeOther) {
            countsLine.append(", ").append(numberFormat.format(restCount));
            labelsLine.append(", ").append("\"Other\"");
        }

        rScript.append(countsLine).append(")\n");
        rScript.append(labelsLine).append("))\n");
        rScript.append("df = data.frame(labels, counts)\n");

    }

    public void appendSort(ChartType chartType, int numberOfSites, boolean includeOther, double restCount) {

        if ( // chartType == ChartType.PIE && 
                includeOther) {
            // if the chart is a pie we temporarily set the count of others to 0 to make sure it ends up at the end of the pie...
            rScript.append("df$counts[").append(numberOfSites).append("] <- 0\n");
        }

        rScript.append("df <- transform(df, "
                + "labels = reorder(labels, ");
        if (chartType == ChartType.PIE) {
            rScript.append("-");
        }
        rScript.append("counts))\n");
        if ( // chartType == ChartType.PIE && 
                includeOther) {
            // if the chart is a pie we set the others count after sorting to make sure it ends up at the end of the pie...
            rScript.append("df$counts[").append(numberOfSites).append("] <- ").append(restCount).append("\n");
        }
    }

    public void setPalette(String paletteName) {
        palette = paletteName;
    }

    public void appendPlots(ChartType chartType, String header, String xlab) {
        if (chartType == ChartType.BAR) {
            // rScript.append("par(las=2)\n"); // make label text perpendicular to axis
            rScript.append("ggplot(df, "
                    + "aes(labels, counts, fill = labels))"
                    + "+ geom_bar(colour=\"White\", show_guide=FALSE)"
                    + "+ xlab(\"").append(xlab).append("\")"
                    + "+ ylab(\"Count\")"
                    + "+ geom_text(aes(label=counts), size = 3)"
                    + "+ opts(title = \"").append(header).append("\")"
                    + "+ scale_fill_discrete(guide=FALSE)"
                    + "+ scale_fill_brewer(palette=\"Paired\")"
                    + "+ coord_flip()\n");
            // TODO: Add an option not to use ggplot?
            // rScript.append("barplot(counts, main=\"").append(header).append("\", horiz=TRUE, names.arg=labels, col=cols)\n");
        } else if (chartType == ChartType.PIE) {
            rScript.append("df$p<-(p<-cumsum(df$counts)-diff(c(0,cumsum(df$counts)))*(1-0.5))\n"); // ref: https://groups.google.com/forum/?fromgroups#!topic/ggplot2/-DsYF8aPjPY
            rScript.append("ggplot(df, "
                    + "aes(x=\"\", y = counts, fill = labels)) "
                    // + "+ scale_fill_discrete()"
                    + "+ scale_fill_brewer(palette=\"").append(palette).append("\", name=\"").append(xlab).append("\") "
                    + "+ geom_bar() " // A hack to hide the slashes: first graph the bars with no outline and add the legend, then graph the bars again with outline, but with a blank legend. ( http://wiki.stdout.org/rcookbook/Graphs/Legends%20(ggplot2)/ ) 
                    + "+ geom_bar(width = 1, colour=\"White\", show_guide=FALSE) "
                    // + "+ guides(fill = guide_legend(reverse=TRUE))"
                    + "+ coord_polar(\"y\")"
                    + "+ ylab(\"Count\")"
                    + "+ geom_text(aes(x= 1.5, y=p, label=counts),vjust=0, size = 3)"
                    + "+ opts(title = \"").append(header).append("\")\n");
            // TODO: Add an option not to use ggplot?
            // rScript.append("pie(counts, labels = labels, main=\"").append(header).append("\", col=cols)\n");
        }
    }

    public void appendWriteOut() {
        rScript.append("dev.off()\n");
    }

    public String getScript() {
        return rScript.toString();
    }
}
