*[← Previous: 12. Technical Details on the Table Builder](12-table-builder-technical-details.md) · [Table of Contents](README.md) · [Next: 14. Scripts & Utilities →](14-scripts.md)*

---

# 13. Third Party Tools

Here’s a list of useful free third-party tools to use with CanReg5.

## Java Runtime Environment

To run CanReg at all you need a Java Runtime Environment. You can get it
from <http://java.com/en/download/manual.jsp>.

### Issues with memory

By default most JVM implementations limit themselves to using a small
amount of RAM (eg: 64MB), even if your computer has a lot of system RAM
(eg: 1GB). This can lead to out of memory conditions, even if your
computer has lots of free memory. To configure your JVM to use more
memory, complete the following steps:

#### On Windows:

Go to Control Panel \| Java , in ’Java’ panel, click view Java applet
running settings. Set Java Running Parameters to:

-Xmx256M -Xms256M -Xss128k

Save and apply these changes. You must close and restart CanReg (and
potential web browsers open) for these changes to take effect.

These settings require 256MB of contiguous memory be available. You may
need to restart your computer before these settings will work.

#### On Solaris/Linux:

<div class="lyxcode">

cd /bin 

./ControlPanel 

</div>

Change Java Applet running Parameters to: -Xmx256M -Xms256M -Xss128k

Press OK, restart CanReg (and potential web browsers open) for these
changes to take effect.

This will configure your JVM to use a fixed 256MB of RAM.

If you have multiple versions of java on same machine, please perform
the above operations for all versions.

## Analysis

### R

R is a free software environment for statistical computing and graphics.
It compiles and runs on a wide variety of UNIX platforms, Windows and
MacOS.

More information about it on: <http://www.r-project.org/>

To download R go to <http://cran.r-project.org/mirrors.html> and choose
a mirror near you, for example <http://cran.univ-lyon1.fr/>. Choose your
platform, i.e. Windows, click the link that says “base”, i.e.
<http://cran.univ-lyon1.fr/bin/windows/base/> and then click “Download
R...”. The latest as of the 18th of October 2016 is version 3.3.1 and
you can get more information on the installation procedure here:
<http://cran.r-project.org/src/base/NEWS.html>.

### Epi Info

From the Epi Info website:

> “Physicians, nurses, epidemiologists, and other public health workers
> lacking a background in information technology often have a need for
> simple tools that allow the rapid creation of data collection
> instruments and data analysis, visualization, and reporting using
> epidemiologic methods. Epi Info™, a suite of lightweight software
> tools, delivers core ad-hoc epidemiologic functionality without the
> complexity or expense of large, enterprise applications.”

More information about it on: <http://wwwn.cdc.gov/epiinfo/>

The newest version of EpiInfo is windows based, but CDC also provides a
DOS based version, Epi Info 6. (Available here:
<http://wwwn.cdc.gov/epiinfo/html/ei6_downloads.htm>. )

### SEER\*Stat and SEER\*Prep

From the SEER website:

> “The SEER\*Stat statistical software provides a convenient, intuitive
> mechanism for the analysis of SEER and other cancer-related databases.
> It is a powerful PC tool to view individual cancer records and to
> produce statistics for studying the impact of cancer on a population.”

To get your data from CanReg to SEER\*Stat you need to go via
SEER\*Prep. (See
<a href="#subsec:CanReg-and-SEER*stat" data-reference-type="ref"
data-reference="subsec:CanReg-and-SEER*stat">[subsec:CanReg-and-SEER*stat]</a>
for more information.)

### PSPP

“PSPP is a program for statistical analysis of sampled data. It is a
Free replacement for the proprietary program SPSS, and appears very
similar to it with a few exceptions.” Available from
<http://www.gnu.org/software/pspp/>.

### SOFA

SOFA - Statistics Open For All The user-friendly, open-source
statistics, analysis, and reporting package. Available from
<http://www.sofastatistics.com/home.php>.

## Data cleaning and record linkage

### Open Refine

Open Refine is a free and open source tool that allows you to
standardise and clean your data prior to importing it into CanReg5.
Please note that even though you access and manipulate your data using a
web browser your data is kept locally on your machine.

More information about it on: <https://github.com/OpenRefine>

### Talend Open Studio

Talend Open Studio is a more heavy duty package to clean and standardise
your data before importing it into CanReg. It is free and open source.

More information about it on: <http://www.talend.com/>

### FRIL: Fine-Grained Records Integration and Linkage Tool

From their website:

> “FRIL is FREE open source tool that enables fast and easy record
> linkage. The tool extends traditional record linkage tools with a
> richer set of parameters. Users may systematically and iteratively
> explore the optimal combination of parameter values to enhance linking
> performance and accuracy. “

More information about it on: <http://fril.sourceforge.net/index.html>

### LinkPlus

From the CDC website:

> “Link Plus is a probabilistic record linkage program developed at
> CDC’s Division of Cancer Prevention and Control in support of CDC’s
> National Program of Cancer Registries (NPCR). Link Plus is a record
> linkage tool for cancer registries. It is an easy-to-use, standalone
> application for Microsoft® Windows® that can run in two modes—
>
> To detect duplicates in a cancer registry database. To link a cancer
> registry file with external files. Although originally designed to be
> used by cancer registries, the program can be used with any type of
> data in fixed width or delimited format. Used extensively across a
> diversity of research disciplines, Link Plus is rapidly becoming an
> essential linkage tool for researchers and organizations that maintain
> public health data.”

More information about it
on:<http://www.cdc.gov/cancer/npcr/tools/registryplus/lp.htm>

## Data security

### GnuPG

- Complete and free implementation of the OpenPGP standard.
  (<http://gnupg.org/>)

- Allows to encrypt and sign your data and communication.

- Easy to use and set up.

- OS independant

  - Gpg4win on Windows <http://www.gpg4win.org>

  - GPGTools on Mac OS X

- Integrates with email clients

  - Enigmail

### TrueCrypt

- Free open-source disk encryption software.

- On-the-fly-encrypted volume (data storage device).

- For Windows 7/Vista/XP, Mac OS X, and Linux.

- <http://www.truecrypt.org>

## Graphics display/editing

### PostScript Viewer 

<div class="flushleft">

Some of the tables generated by CanReg5 are PostScipt files. PostScript
is an open standard, so you can use many different tools to view them.
(You can in many cases even send them directly to a printer.) Mac OS X
and most Linux-distributions come with tools to view them by default.

</div>

<div class="flushleft">

On Windows, the tool I recommend is the open sourced and free GSview.

</div>

<div class="flushleft">

To run GS View you need to install Ghostscript first. This can be for
example be downloaded from here:
<http://www.ghostscript.com/download/gsdnld.html> (Look under the
heading “Platform” and download the “GPL Ghostscript 9.xx for Windows
(32-bit)” AGPL Release.

</div>

<div class="flushleft">

Run this file to install Ghostscript.

</div>

<div class="flushleft">

Then you can get GS View from here:
[http://pages.cs.wisc.edu/$`\sim`$ghost/gsview/get50.htm](http://pages.cs.wisc.edu/~ghost/gsview/get50.htm)
(Most probably, you should pick the Win32 self extracting archive - the
first download option.)

</div>

<div class="flushleft">

Run this file to install GS View.

</div>

### InkScape

InkScape is a free open source program to display and edit SVGs and
other vector based files.

From their website:

> “An Open Source vector graphics editor, with capabilities similar to
> Illustrator, CorelDraw, or Xara X, using the W3C standard Scalable
> Vector Graphics (SVG) file format.
>
> Inkscape supports many advanced SVG features (markers, clones, alpha
> blending, etc.) and great care is taken in designing a streamlined
> interface. It is very easy to edit nodes, perform complex path
> operations, trace bitmaps and much more.”

Available from: <http://inkscape.org/>

## General

### Notepad++

Notepad++ is an excellent open source text editor that is perfect for
writing code, cleaning a dataset using, for example regular expressions,
or just simply looking at data from CanReg. Available from:
<http://notepad-plus-plus.org/> (It has nothing to do with the Notepad
that is distributed with Windows - it merely shares the first part of
it’s name with it.)

---

*[← Previous: 12. Technical Details on the Table Builder](12-table-builder-technical-details.md) | [Table of Contents](README.md) | [Next: 14. Scripts & Utilities →](14-scripts.md)*
