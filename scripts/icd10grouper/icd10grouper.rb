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
options[:conf_file_name] = "Groups.conf"
options[:canreg_path] = '../../CanReg.jar'

# overrides
option_parser = OptionParser.new do |opts|
  executable_name = File.split($0)[1]
  opts.banner = "Add column of icd10, groups and group defs for incidence data in a spreadsheet.
  Usage: jruby --1.9 #{executable_name} [options]
  Example: jruby --1.9 #{executable_name} -t Topography -m Morphology -b Behaviour -s Sex -c , -g Groups.conf -f LotsOfData.csv
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
  opts.on("-g GROUP_CONF_FILE", "--group-conf-file GROUP_CONF_FILE", "File containing the definitions of the groups.\n Default: #{options[:conf_file_name]}") do |column|
    options[:conf_file_name] = column
  end
  opts.on("-p PATH_TO_CANREG","--path-to-canreg PATH_TO_CANREG", "Path to the CanReg.jar-file.\n Default: #{options[:canreg_path]}") do |path|
    options[:canreg_path] = path
  end
  opts.on("-f IN_FILE", "--in-file IN_FILE", "File to process.\n Default: #{options[:in_file_name]}") do |file|
    options[:in_file_name] = file
    fileparts = file.split(".")
    # puts fileparts
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

require options[:canreg_path]

java_import 'canreg.client.analysis.EditorialTableTools'
java_import 'canreg.common.conversions.ConversionICDO3toICD10'
java_import 'canreg.common.Globals'
java_import 'java.util.HashMap'

conversion = ConversionICDO3toICD10.new
# conversion.getVariablesNeeded.each {|v| puts v}

puts "Reading confing file: #{options[:conf_file_name]}"

config = {}
conf_strings = []
conf_name = ''
File.open(options[:conf_file_name]).each do |line|
  if line=~/{/
    conf_strings = []
    conf_name = line.delete('{').strip
    # puts conf_name
  elsif line=~/}/
    config[conf_name] = conf_strings
    # puts "hello"
  else
    line = line.delete('"').strip
    conf_strings.push line if line.length>0
    # puts line
  end
end
group_labels = config["ICD_groups_labels"].map { |entry| entry[3..-1]}

group_definitions = config["ICD10_groups"]
groups = EditorialTableTools.generateICD10Groups(group_definitions)
# groups.each {|g| puts g}

in_file = CSV.open(options[:in_file_name], mode = "rb", headers: true, col_sep: options[:separating_character])
out_file = CSV.open(options[:out_file_name], mode = "wb", headers: true, col_sep: options[:separating_character])

puts "Processing file: #{options[:in_file_name]}"
puts "Writing to: #{options[:out_file_name]}"

idx = 0
in_file.each do |line|
  if (idx==0)
    headers = line.headers
    [options[:top_column], options[:mor_column], options[:beh_column], options[:sex_column]].each do |column|
      unless headers.include? column
        puts "No such column name: #{column}.\n---\n"
        puts option_parser.help
        exit 1
      end
    end
    headers << "icd10" << "group" << "label"
    out_file.puts(headers)
  end
  
  map = HashMap.new
  map.put(Globals::StandardVariableNames::Sex, line[options[:sex_column]])
  map.put(Globals::StandardVariableNames::Topography, line[options[:top_column]].delete("C"))
  map.put(Globals::StandardVariableNames::Morphology, line[options[:mor_column]])
  map.put(Globals::StandardVariableNames::Behaviour, line[options[:beh_column]])
  icd10 = conversion.performConversion(map)[0].getValue
  
  line.push icd10
  group_index = nil
  if icd10.include?("C")
    while icd10.length<4
      icd10 << "0"
    end
    group_index = EditorialTableTools.getICD10index(icd10.delete("C").to_i, groups)
    group_index = group_definitions.index("O&U") if group_index<0
  elsif icd10.include?("D")  
    while icd10.length<4
      icd10 << "0"
    end
    icdNumber = icd10.delete("D").to_i
    if (icdNumber == 90 || icdNumber == 414)
      group_index = EditorialTableTools.getICD10index(670, groups)
    elsif ((icdNumber / 10) == 45 || (icdNumber / 10) == 47)
      group_index = group_definitions.index("MPD")
    elsif ((icdNumber / 10) == 46)
      group_index = group_definitions.index("MDS")
    end
  end
  if (!group_index.nil? && group_index>=0)
    line.push group_labels[group_index]
    line.push group_definitions[group_index]
  else
    line.push "Not countable"
    line.push "N/A"    
  end
  out_file << line
  idx += 1
  print "+" if (idx%1000==0)
end
puts "\nThank you for using Morten's ICD10-grouper."