# Written by Morten Ervik <ervikm@iarc.fr>.
# 
# This program is distributed under the terms of the CanReg5 license.
# You can freely distribute/modify this library.
# 
# It is distributed with CanReg5.
#

require 'java'
require 'csv'
require 'optparse'

options = {}

## default values
options[:top_column] = "TOP"
options[:mor_column] = "MOR"
options[:beh_column] = "BEH"
options[:sex_column] = "SEX"
options[:separating_character] = "\t"
options[:in_file_name] = "Lots of Data.txt"
options[:out_file_name] = options[:in_file_name].split(".")[0]+"-out.txt"
options[:canreg_path] = '../../CanReg.jar'

# overrides
option_parser = OptionParser.new do |opts|
  executable_name = File.split($0)[1]
  opts.banner = "Add column of icd10, iccc3, for incidence data in a spreadsheet.
  Usage: jruby --1.9 #{executable_name} [options]
  Example: jruby --1.9 #{executable_name} -t Topography -m Morphology -b Behaviour -s Sex -c , -f LotsOfData.csv
  "
  # Create a switch
  opts.on("-t TOPOGRAPHY_COLUMN_NAME","--topography-column-name TOPOGRAPHY_COLUMN_NAME", "Name of the column containing topography.\n Default: #{options[:top_column]}") do |column|
    options[:top_column] = column
  end
  opts.on("-m MORPHOLOGY_COLUMN_NAME","--morphology-column-name MORPHOLOGY_COLUMN_NAME", "Name of the column containing morphology.\n Default: #{options[:mor_column]}") do |column|
    options[:mor_column] = column
  end
  opts.on("-b BEHAVIOUR_COLUMN_NAME","--behaviour-column-name BEHAVIOUR_COLUMN_NAME", "Name of the column containing behaviour.\n Default: #{options[:beh_column]}") do |column|
    options[:beh_column] = column
  end
  opts.on("-s SEX_COLUMN_NAME","--sex-column-name SEX_COLUMN_NAME", "Name of the column containing sex. (Coded: 1-Male, 2-Female)\n Default: #{options[:sex_column]}") do |column|
    options[:sex_column] = column
  end
  opts.on("-c SEPARATING_CHARACTER", "--separating-character SEPARATING_CHARACTER", "Separating character in the files.\n Default: #{options[:separating_character]=="\t" ? "tab" : options[:separating_character]}") do |char|
    options[:separating_character] = char
  end
  opts.on("-p PATH_TO_CANREG","--path-to-canreg PATH_TO_CANREG", "Path to the CanReg.jar-file.\n Default: #{options[:canreg_path]}") do |path|
    options[:canreg_path] = path
  end
  opts.on("-f IN_FILE", "--in-file IN_FILE", "File to process.\n Default: #{options[:in_file_name]}") do |file|
    options[:in_file_name] = file
    fileparts = file.split(".")
    fileparts.length>1 ? fileparts[-2] = fileparts[-2]+"-out" : fileparts[0] = fileparts[0]+"-out"
    options[:out_file_name] = fileparts.join(".")
  end
  opts.on("-h", "--help", "Display help functions.") do 
    puts option_parser.help
    exit 0
  end
end

begin
  option_parser.parse!
rescue OptionParser::InvalidArgument => ex
  STDERR.puts ex.message
  STDERR.puts option_parser
end

unless File.exist?(options[:in_file_name])
  puts "ERROR: No such file: #{options[:in_file_name]}.\n---\n"
  puts option_parser.help
  exit 1
end

# load in the CanReg.jar
require options[:canreg_path]

java_import 'canreg.client.analysis.EditorialTableTools'
java_import 'canreg.common.conversions.ConversionICDO3toICD10'
java_import 'canreg.common.conversions.ConversionICDO3toICCC3'
java_import 'canreg.common.Globals'
java_import 'java.util.HashMap'

icd10_conversion = ConversionICDO3toICD10.new
iccc_conversion = ConversionICDO3toICCC3.new

in_file = CSV.open(options[:in_file_name], mode = "rb", headers: true, col_sep: options[:separating_character])
out_file = CSV.open(options[:out_file_name], mode = "wb", headers: true, col_sep: options[:separating_character])

puts "Processing file: #{options[:in_file_name]}"
puts "Writing to: #{options[:out_file_name]}"

idx = 0
in_file.each do |line|
  if (idx==0)
    headers = line.headers
    puts headers
    [options[:top_column], options[:mor_column], options[:beh_column], options[:sex_column]].each do |column|
      unless headers.include? column
        puts "No such column name: #{column}.\n---\n"
        puts option_parser.help
        exit 1
      end
    end
    headers << "icd10" << "iccc3"
    out_file.puts(headers)
  end
  
  map = HashMap.new
  map.put(Globals::StandardVariableNames::Sex, line[options[:sex_column]])
  top = line[options[:top_column]].delete("C")
  while top.length<3
    top = "0" + top
  end
  map.put(Globals::StandardVariableNames::Topography, top)
  map.put(Globals::StandardVariableNames::Morphology, line[options[:mor_column]])
  map.put(Globals::StandardVariableNames::Behaviour, line[options[:beh_column]])
  icd10 = icd10_conversion.performConversion(map)[0].getValue
  iccc = iccc_conversion.performConversion(map)[0].getValue
  
  line.push icd10
  line.push iccc

  out_file << line
  idx += 1
  print "+" if (idx%1000==0)
end
puts "\nThank you for using Morten's ICD10&ICCC-converter."