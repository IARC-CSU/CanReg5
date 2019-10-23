# Source Splitter

Splits source records.

## Prerequisites:

### Install JRuby
1) Downoad the latest JRuby from here: http://jruby.org/download fitting your machine
2) Run the installer and follow on screen instructions. (Make sure you add jruby to the path when asked.)
3) - Windows: Run the install.bat in this folder.
   - Linux/Mac: run the following commands
     - ```jgem install bundler```
     - ```jruby -S bundle install```

## Usage

```bash
Split CanReg5 data records with multiple sources into multiple records.
  Usage: jruby splitter.rb [options]
  Example: jruby splitter.rb -c ',' -f LotsOfData.csv
  
    -c SEPARATING_CHARACTER,         Separating character in the files.
 Default: tab
        --separating-character
    -f, --in-file IN_FILE            File to process.
 Default: Lots of Data.txt
    -m, --map-file MAP_FILE          JSON file with variable mapping.
 Default: conf.json
    -h, --help                       Display help functions.

```

### Example mapping file

```js
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
```
