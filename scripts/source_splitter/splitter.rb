# Written by Morten Ervik <ervikm@iarc.fr>.
# 
# This program is distributed under the terms of the CanReg5 license.
# You can freely distribute/modify this library.
# 
# It is distributed with CanReg5.
#

require 'csv'
require 'optparse'

options = {}

## default values
options[:separating_character] = "\t"
options[:in_file_name] = "Lots of Data.txt"
options[:out_file_name] = options[:in_file_name].split(".")[0]+"-out.txt"

# overrides
option_parser = OptionParser.new do |opts|
  executable_name = File.split($0)[1]
  opts.banner = "Split CanReg5 data records with multiple sources into multiple records.
  Usage: ruby #{executable_name} [options]
  Example: ruby #{executable_name} -c ',' -f LotsOfData.csv
  "
  # Create a switch
  opts.on("-c SEPARATING_CHARACTER", "--separating-character SEPARATING_CHARACTER", "Separating character in the files.\n Default: #{options[:separating_character]=="\t" ? "tab" : options[:separating_character]}") do |char|
    options[:separating_character] = char
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

source_fields_map    = [ 
  {
    "HOSP1" => "HOSP",
    "DATE1" => "SDATE",
    "HOSPNO1" => "REFNO"
  },
  {
    "HOSP2" => "HOSP",
    "DATE2" => "SDATE",
    "REFNO2" => "REFNO"
  },
  {
    "HOSP3" => "HOSP",
    "DATE3" => "SDATE",
    "REFNO3" => "REFNO"
  }
]

common_fields_map = {
  "SERONO" => "SERONO",
  "HISTONO" => "HISTONO",
  "OTHERLNO" => "OTHERLNO",
  "SOURCE" => "SOURCE"
}

tumour_id_field = "TUMOURIDSOURCETABLE"
source_id_field = "SOURCERECORDID"

in_file = CSV.open(options[:in_file_name], mode = "rb", headers: true, col_sep: options[:separating_character])
out_file = CSV.open(options[:out_file_name], mode = "wb", headers: true, col_sep: options[:separating_character])

puts "Processing file: #{options[:in_file_name]}"
puts "Writing to: #{options[:out_file_name]}"

in_file_headers = source_fields_map.map(&:keys).flatten.uniq + common_fields_map.values + [tumour_id_field, source_id_field]
out_file_headers = source_fields_map.map(&:values).flatten.uniq + common_fields_map.values + [tumour_id_field, source_id_field]
puts in_file_headers.join(",")
puts out_file_headers.join(",")


idx = 0
in_file.each do |line|
  if (idx==0)
    headers = line.headers
    puts "Number of comlumns in in-file: #{headers.length}"
    puts headers.join(",")
    in_file_headers.each do |column|
      unless headers.include? column
        puts "No such column name: #{column}.\n---\n"
        puts option_parser.help
        exit 1
      end
    end
    out_file.puts(out_file_headers)
  end

  source_fields_map.each_with_index do | map, index |
    new_line = {}
    data_found = ""
    map.each do |k,v| 
      new_line[v] = line [k]
      data_found += line[k] unless line[k].nil?
    end
    if data_found.strip.length>0
      common_fields_map.each do |k,v| 
        new_line[v] = line [k]
      end
      
      new_line[tumour_id_field] = line[tumour_id_field]
      new_line[source_id_field] = line[tumour_id_field] + (index + 1).to_s.rjust(2, "0")

      out_file << new_line
    else
      # puts "No data..."
    end
  end

  idx += 1
  print "+" if (idx%1000==0)
end