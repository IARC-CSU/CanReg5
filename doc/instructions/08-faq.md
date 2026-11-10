*[← Previous: 7. Management & Quality Control](07-management.md) · [Table of Contents](README.md) · [Next: 9. Known Issues →](09-known-issues.md)*

---

# 8. Frequently Asked Questions

## Server 

Q:  
<div class="flushleft">

When I click the “Launch Server”/“Test Connection” button, it takes
**more than 3 minutes** to launch the server and I get the message that
the “Server \[is\] already running”. Afterwards I cannot log in.

</div>

A:  
<div class="flushleft">

Xenios found the following solution: On our PCs, we use Microsoft
Internet Explorer 7. In the “Tools / Internet Options / Connections /
LAN Settings” we have a tick in the checkbox for “Use a proxy server for
your LAN” and the address and port of the proxy server filled in
accordingly. I put a tick in the checkbox for “Bypass proxy server for
local addresses”, clicked the “Advanced” button and typed the IP address
of the PC (localhost) in the “Do not use proxy server for addresses
beginning with:” box.

</div>

Q:  
<div class="flushleft">

When I try to log on to the CanReg server I get the message: “Exception
creating connection to: aaa.bbb.ccc.ddd; nested exception is:
java.net.SocketException: Malformed reply from SOCKS server”

</div>

A:  
<div class="flushleft">

This can be solved using the same procedure as the previous answer.

</div>

Q:  
<div class="flushleft">

Is CanReg5 only designed for local networks or does it allow remote
registration?

</div>

A:  
<div class="flushleft">

CanReg5 is designed for local networks, yes, but traffic is over
standard TCP/IP so you can tunnel this through secure channels (SSH/VPN
etc) to do remote registration. (This is not "officially" supported, as
we haven’t been able to test it enough (yet).)

</div>

## Conversion CanReg4 to CanReg5

Q:  
<div class="flushleft">

In what table should the variable age be stored?

</div>

A:  
<div class="flushleft">

The tumour table. Like that, if the same patient has a new tumour you
can (probably) keep the patient record and just add a new tumour record.
Birth date is stored in the patient table. (Incidence date with the
tumour.)

</div>

Q:  
<div class="flushleft">

Do I need to “install” the CanReg5 system definition file after
converting from CanReg4 using the built in tool?

</div>

A:  
<div class="flushleft">

After converting the system you don’t need to “install” it afterwards as
the XML file is automatically copied to your system folder during
conversion.

</div>

Q:  
<div class="flushleft">

I get errors during import of the data from CanReg4. The process stops
after a certain percentage every time.

</div>

A:  
<div class="flushleft">

Try exporting your data with a comma separated variables instead of the
default tab-separated ones (or vice versa) and see if that helps.

</div>

Q:  
<div class="flushleft">

In CanReg5, how do you clear previous data and import fresh ones from
canreg4.

</div>

A:  
<div class="flushleft">

To clear the previous data from CanReg5 you need to delete the database
files. These can be found in your user folder under
`.CanRegServer\Database` (On my machine it is under `C:\Documents and Settings\ervikm\.CanRegServer\Database`). Please note that this deletes
the dictionaries and population data as well, so you might want to
export those prior to deleting this. Then you can relaunch CanReg and
the server and the empty database will be rebuilt. It is a good idea to
keep a backup of an empty database - just containing dictionaries (and
population data) to get back to this state of the database if needed.

</div>

## Dictionary

Q:  
<div class="flushleft">

Can I import dictionaries from other CanReg systems to my own?

</div>

A:  
<div class="flushleft">

Since most CanReg systems have different dictionary structure (length of
codes, order of dictionaries etc.) you need to import the dictionary
corresponding to your system or do necessary modifications.

</div>

## Analysis/tables

Q:  
<div class="flushleft">

What program can I use to view the post script files with?

</div>

A:  
<div class="flushleft">

PostScript is an open standard, so you can use many different tools to
view them. (You can in many cases even send them directly to a printer.)
Apple’s OSX and most Linux-distributions (Ubuntu, RedHat, SuSE etc) come
with a tool to view them by default. On Windows the tool I recommend is
the open sourced and free GSview. (Available from:
[http://pages.cs.wisc.edu/$`\sim`$ghost/gsview/](http://pages.cs.wisc.edu/~ghost/gsview/)
) To run GS View you need to install Ghostscript first. This can be
downloaded from here:
[http://pages.cs.wisc.edu/$`\sim`$ghost/doc/GPL/gpl864.htm](http://pages.cs.wisc.edu/~ghost/doc/GPL/gpl864.htm)
(Scroll all the way down, under the heading Microsoft windows and
download the “GPL Ghostscript 8.64 for 32-bit Windows (the common
variety)”
(<http://mirror.cs.wisc.edu/pub/mirrors/ghost/GPL/gs864/gs864w32.exe>)
Run this file to install Ghostscript. Then you can get GSView from here:
[http://pages.cs.wisc.edu/$`\sim`$ghost/gsview/get49.htm](http://pages.cs.wisc.edu/~ghost/gsview/get49.htm)
(Most probably, you should pick the Win32 self extracting archive - the
first download option.
<http://mirror.cs.wisc.edu/pub/mirrors/ghost/ghostgum/gsv49w32.exe>) Run
this file to install GSView.

</div>

## Import

Q:  
<div class="flushleft">

Some letters are distorted/missing in records after an import. (For
example when importing Arabic names.) Why is that?

</div>

A:  
<div class="flushleft">

If the data is from a program that does not code the data using Unicode
(for example previous versions of CanReg) you need to specify the coding
scheme/“codepage” during import of that file to your database. If you
pick the wrong one your data might get distorted. To solve this problem
you need to re-import the data. Please use the preview button during
import to see to that you have the right coding scheme.

</div>

Q:  
<div class="flushleft">

While importing my data the process brakes down before the end and I get
an error message saying that there is something wrong with the file.

</div>

A:  
<div class="flushleft">

First of all make sure that you have the latest version of CanReg
installed, as some of the earlier versions had a bug that manifested
itself while importing files. Then look at the line where the import
brakes down and see if you have some unclosed quotes in any of the
fields. This will brake down the import as CanReg allows qoutes around
strings that contain the separating character, but not unclosed quotes.\
Another possibility is that your Java Runtime Environment has been set
up with not enough memory. To address this please see
<a href="#subsec:Issues-with-memory" data-reference-type="ref"
data-reference="subsec:Issues-with-memory">[subsec:Issues-with-memory]</a>.

</div>

## Database design

Q:  
<div class="flushleft">

What is the minimum data set that one would collect?

</div>

A:  
<div class="flushleft">

The minimum data set required is up to your registry to define. Please
refer to [this
publication](http://www.iarc.fr./en/publications/pdfs-online/epi/sp95/index.php)
for more information: ([Chapter
6](http://www.iarc.fr./en/publications/pdfs-online/epi/sp95/sp95-chap6.pdf),
for example. ) The bare minimum are detailed in Figure
<a href="#fig:Minimum-variables." data-reference-type="ref"
data-reference="fig:Minimum-variables.">1.1</a>.

<figure id="fig:Minimum-variables.">
<ul>
<li><p>Name(s):</p>
<ul>
<li><p>String, According to local usage, but at least one, eg First
name, Middle name, Last Name, Father’s name, Grandfather’s name, Maiden
Name.</p></li>
</ul></li>
<li><p>Gender/Sex:</p>
<ul>
<li><p>Coded, 1 Male; 2 Female; 9 Unknown; (Some registries use 3 for
Other.)</p></li>
</ul></li>
<li><p>Birth date:</p>
<ul>
<li><p>String, Stored in the database as yyyyMMdd (but can, in
principle, be entered in any format), Allows 99 for unknown day and 99
for unknown month.</p></li>
</ul></li>
<li><p>Incidence date:</p>
<ul>
<li><p>String, Stored in the database as yyyyMMdd (but can, in
principle, be entered in any format), Allows 99 for unknown day and 99
for unknown month.</p></li>
</ul></li>
<li><p>Address:</p>
<ul>
<li><p>Coded, According to local usage/needs.</p></li>
</ul></li>
<li><p>Ethnic group (optional, but in some cases encouraged):</p>
<ul>
<li><p>Coded, According to local usage/needs.</p></li>
</ul></li>
<li><p>Age:</p>
<ul>
<li><p>Integer (For legacy reasons this is input as 3 digits, with 999
as unknown. Some registries collect only two digits so unknown age is
coded as 99 and 98 and greater is coded 98.)</p></li>
</ul></li>
<li><p>(Most valid) Basis of diagnosis:</p>
<ul>
<li><p>Coded, (1,2,3,4) Non-microscopic, (5,6,7,8) Microscopic, 9
Unknown, 0 Death certificate only.</p></li>
</ul></li>
<li><p>Source of information:</p>
<ul>
<li><p>Coded, According to local usage, eg hospital number.</p></li>
</ul></li>
<li><p>Source of information - additional information:</p>
<ul>
<li><p>String, According to local usage, eg hospital record number, name
of physician.</p></li>
</ul></li>
</ul>
<p>Note: Registries can choose to collect Date of Birth OR Age, but most
choose to collect both.</p>
<figcaption><span id="fig:Minimum-variables."
data-label="fig:Minimum-variables."></span>Minimum
variables.</figcaption>
</figure>

</div>

Q:  
<div class="flushleft">

In what database table should the variable X be stored?

</div>

A:  
<div class="flushleft">

In the **patient** table you store the information about the patient
that never changes (or at least very seldom) in the patient table. (That
is birth date, sex, names etc.) You also store follow up information in
the patient table. (Vital status, current address, etc.) In the
**tumour** table you store all data that can potentially differ between
two tumours of the same patient. (Topography, morphology, age, address
at the time of the tumour (coded) etc.) Finally, in the **source** table
you store all information about the sources of information of any given
tumour. (Source name (preferably coded), source type, date etc.)

</div>

Q:  
<div class="flushleft">

After adding a new/removing an old variable to collect in the database
what do I need to do?

</div>

A:  
<div class="flushleft">

First of all, before adding or removing variables using the built in
tool or editing the underlying XML file you should take a backup of your
old database. Then you need to export all the data - one table at a time
from CanReg5 - so that you have 3 files. One for the patient table, one
for the tumour table and one for the source table. Then you need to
export the dictionaries. After editing the database structure you will
have to delete the old database files, relaunch CanReg5 and import these
files to the empty database. Basically what CanReg does when you launch
the server is first to load the XML describing the database, then checks
to see if the database files exist in the Database folder. If this does
exist it checks to see if it corresponds with the XML. If it does not it
will not be able to launch the server. (I’ll look into creating a better
set of error messages.) If it does not find a database folder with the
registry code found in the XML it will generate an empty one.

</div>

## Install/update CanReg5

Q:  
<div class="flushleft">

Parts of CanReg5 are no longer working after upgrading to the latest
version. What should I do?

</div>

A:  
<div class="flushleft">

First, uninstall CanReg5 and reinstall the latest version. See if that
helps. If not write a bug report to canreg@iarc.fr.

</div>

---

*[← Previous: 7. Management & Quality Control](07-management.md) | [Table of Contents](README.md) | [Next: 9. Known Issues →](09-known-issues.md)*
