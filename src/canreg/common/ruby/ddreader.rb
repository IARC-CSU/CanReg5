# ddfile = "C:\\Program Files (x86)\\SEERPrep\\naaccr3339.ver12_1.d01252011.dd"
# ddfile = "C:\\Program Files (x86)\\SEERPrep\\naaccr1946.ver11_3.d02032011.dd"
ddfile = "./naaccr1946.ver11_3.d02032011.dd"
out_file = File.new("export_format_naaccr1946.ver11_3.d02032011.tsv","w+")

class Ddelement  
  attr_accessor :name, :std_name, :case_col, :pop_col, :length, :required, :derived
  def initialize (derived)
    @derived = derived
    @reuired = false
  end
  def to_s
    [@name, @case_col, @pop_col, @length, @required, @std_name].join("\t")
  end
  def self.get_file_header
    ["name", "case_col", "pop_col", "length", "required", "std_name"].join("\t")
  end
end

standard_variables_map = Hash.new("na")

# read map from file
File.open("export_std_variables.tsv") do |file|
  file.readline # skip the first line
  file.each do |line|
    elems = line.split("\t")
    standard_variables_map[elems[0]]=elems[1]
  end
end

# prep the out file
out_file.puts(Ddelement.get_file_header)

ddelement = nil

File.open(ddfile).each do |line|
  if /\[(.*)Field[0-9]*/=~line
    # write previous one
    out_file.puts ddelement unless (ddelement == nil || !ddelement.required || ddelement.derived)
    # create new one
    ddelement = Ddelement.new($1.strip=="Derived")
  end
  if /^ByteOffset=([0-9]*)$/=~line
    ddelement.case_col = $1.to_i
  elsif /^PopByteOffset=([0-9]*)$/=~line
    ddelement.pop_col = $1.to_i
  elsif /^Length=([0-9]*)$/=~line
    ddelement.length = $1.to_i
  elsif /^FieldName=(.*)$/=~line
    name = $1.strip
    ddelement.name = name
    ddelement.std_name = standard_variables_map[name]
  elsif /^Required=([a-z]*)$/=~line
    ddelement.required = ($1=='true')
  end
end

# add two lines that we don't pick up from conf yet...
out_file.puts("Age recode with <1 year olds\t\t17\t2\ttrue\tAgeGroup")
out_file.puts("Count\t\t19\t8\ttrue\tCount")

puts "Written to #{out_file.path}"