# ICD10 converter and grouper

Add column of icd10, groups and group defs for incidence data in a spreadsheet.

This tool lets you add icd10 codes and user defined groups to data in a csv file.

## Prerequisites:

### Install JRuby
1) Downoad the latest JRuby from here: http://jruby.org/download fitting your machine
2) Run the installer and follow on screen instructions. (Make sure you add jruby to the path when asked.)
3) 
  - Windows: Run the install.bat in this folder.
  - Linux/Mac: run the following commands
    - ```jgem install bundler```
    - ```jruby -S bundle install```

## Usage

```bash
  Usage: jruby icd10grouper.rb [options]
  Example: jruby icd10grouper.rb -t Topography -m Morphology -b Behaviour -s Sex -c , -g Groups.conf -f LotsOfData.csv
  
    -t TOPOGRAPHY_COLUMN_NAME,       Name of the column containing topography.
 Default: TOP
        --topography-column-name
    -m MORPHOLOGY_COLUMN_NAME,       Name of the column containing morphology.
 Default: MOR
        --morphology-column-name
    -b BEHAVIOUR_COLUMN_NAME,        Name of the column containing behaviour.
 Default: BEH
        --behaviour-column-name
    -s SEX_COLUMN_NAME,              Name of the column containing sex. (Coded: 1-Male, 2-Female)
 Default: SEX
        --sex-column-name
    -c SEPARATING_CHARACTER,         Separating character in the files.
 Default: tab
        --separating-character
    -g GROUP_CONF_FILE,              File containing the definitions of the groups.
 Default: Groups.conf
        --group-conf-file
    -p PATH_TO_CANREG,               Path to the CanReg.jar-file.
 Default: ../../CanReg.jar
        --path-to-canreg
    -f, --in-file IN_FILE            File to process.
 Default: 1993-2007 Izmir Data ME.txt
    -h, --help                       Display help functions.

```

See the CanReg5 handbook, run.bat, Groups.conf and icd10grouper.rb for more information.