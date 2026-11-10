*[← Previous: 13. Third Party Tools](13-third-party-tools.md) · [Table of Contents](README.md) · [Next: 15. About & Acknowledgments →](15-about.md)*

---

# 14. Scripts & Utilities

## Ruby scripts

### Script to generate icd10 codes and cancer groups

This tool lets you add icd10 codes and user defined groups to data in a
csv file.

#### Prerequisites

##### Install JRuby 

1.  Downoad the latest JRuby from here: http://jruby.org/download
    fitting your machine

2.  Run the installer and follow on screen instructions. (Make sure you
    add jruby to the path when asked.)

3.  Windows: Run the “install.bat” in the script folder. Linux/Mac: run
    the following commands:

<div class="lyxcode">

jgem install bundler 

jruby --1.9 -S bundle install

</div>

#### Usage

<div class="lyxcode">

jruby --1.9 icd10grouper.rb \[options\] 

</div>

##### Example: 

<div class="lyxcode">

jruby --1.9 icd10grouper.rb -t Topography -m Morphology -b Behaviour 

-s Sex -c , -g Groups.conf -f LotsOfData.csv

</div>

##### Arguments:

<div class="lyxcode">

-t TOPOGRAPHY_COLUMN_NAME, Name of the column containing topography. 

Default: TOP --topography-column-name

-m MORPHOLOGY_COLUMN_NAME, Name of the column containing morphology. 

Default: MOR --morphology-column-name

-b BEHAVIOUR_COLUMN_NAME, Name of the column containing behaviour. 

Default: BEH --behaviour-column-name 

-s SEX_COLUMN_NAME, Name of the column containing sex. 

(Coded: 1-Male, 2-Female) Default: SEX --sex-column-name 

-c SEPARATING_CHARACTER, Separating character in the files. 

Default: tab --separating-character 

-g GROUP_CONF_FILE, File containing the definitions of the groups. 

Default: Groups.conf --group-conf-file 

-p PATH_TO_CANREG, Path to the CanReg.jar-file. 

Default: ../../CanReg.jar --path-to-canreg 

-f, --in-file IN_FILE File to process. 

Default: “Here be Data.txt” 

-h, --help Display help functions.

</div>

### Script to generate iccc codes

Add column of icd10 and iccc3 for incidence data in a spreadsheet.

#### Prerequisites

##### Install JRuby 

1.  Downoad the latest JRuby from here: http://jruby.org/download
    fitting your machine

2.  Run the installer and follow on screen instructions. (Make sure you
    add jruby to the path when asked.)

3.  Windows: Run the “install.bat” in the script folder. Linux/Mac: run
    the following commands:

<div class="lyxcode">

jgem install bundler 

jruby --1.9 -S bundle install

</div>

#### Usage

<div class="lyxcode">

jruby icd10_iccc_converter.rb \[options\] 

</div>

##### Example: 

<div class="lyxcode">

jruby icd10_iccc_converter.rb -t Topography -m Morphology -b Behaviour -s Sex -c , -f LotsOfData.csv

</div>

##### Arguments:

<div class="lyxcode">

-t TOPOGRAPHY_COLUMN_NAME,       Name of the column containing topography.  Default: TOP         --topography-column-name     

-m MORPHOLOGY_COLUMN_NAME,       Name of the column containing morphology.  Default: MOR         --morphology-column-name     

-b BEHAVIOUR_COLUMN_NAME,        Name of the column containing behaviour.  Default: BEH         --behaviour-column-name     

-s SEX_COLUMN_NAME,              Name of the column containing sex. (Coded: 1-Male, 2-Female)  Default: SEX         --sex-column-name     

-c SEPARATING_CHARACTER,         Separating character in the files.  Default: tab         --separating-character     

-p PATH_TO_CANREG,               Path to the CanReg.jar-file.  Default: ../../CanReg.jar         --path-to-canreg     

-f, --in-file IN_FILE            File to process.  Default: Lots of Data.txt     

-h, --help                       Display help functions.

</div>

### Script to split source records

Split CanReg5 data records with multiple sources into multiple records.

#### Prerequisites

To use this tool you need to either install an implementation of Ruby --
JRuby or regular ruby is recommended.

##### Install JRuby

1.  Downoad the latest JRuby from here: http://jruby.org/download
    fitting your machine

2.  Run the installer and follow on screen instructions. (Make sure you
    add jruby to the path when asked.)

3.  Windows: Run the “install.bat” in the script folder. Linux/Mac: run
    the following commands:

<div class="lyxcode">

jgem install bundler 

jruby -S bundle install

</div>

##### Install JRuby

1.  Downoad the latest Ruby from here: http://ruby.org/download fitting
    your machine

2.  Run the installer and follow on screen instructions. (Make sure you
    add jruby to the path when asked.)

3.  Run the following commands:

<div class="lyxcode">

gem install bundler 

bundle install

</div>

#### Usage

<div class="lyxcode">

jruby splitter.rb \[options\] 

</div>

##### Example: 

<div class="lyxcode">

jruby splitter.rb -c ’,’ -f LotsOfData.csv

</div>

##### Arguments:

<div class="lyxcode">

-c SEPARATING_CHARACTER,         Separating character in the files.  Default: tab         --separating-character     

-f, --in-file IN_FILE            File to process.  Default: Lots of Data.txt     

-m, --map-file MAP_FILE          JSON file with variable mapping.  Default: conf.json     

-h, --help                       Display help functions.

</div>

##### Example mapping file

    {   
      "tumour_id_field": "TUMOURIDSOURCETABLE",
      "source_id_field": "SOURCERECORDID",   
      "common_fields_map": {
        "SERONO": "SERONO",
        "HISTONO": "HISTONO",
        "OTHERLNO": "OTHERLNO",
        "SOURCE": "SOURCE"
      },
      "source_fields_map": [
        {
          "HOSP1": "HOSP",
          "DATE1": "SDATE",
          "HOSPNO1": "REFNO"
        },
        {
          "HOSP2": "HOSP",
          "DATE2": "SDATE",
          "REFNO2": "REFNO"
        },
        {
          "HOSP3": "HOSP",
          "DATE3": "SDATE",
          "REFNO3": "REFNO"
        }
      ]
    }

---

*[← Previous: 13. Third Party Tools](13-third-party-tools.md) | [Table of Contents](README.md) | [Next: 15. About & Acknowledgments →](15-about.md)*
