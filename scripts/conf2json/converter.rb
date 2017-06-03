require "JSON"

file_list = []

def load_conf(infile)
  text = File.open(infile).read
  blocks = {}
  text.split("}").each do |elems|
    lines = elems.lines.delete_if {|e| e.strip.empty? }
    unless lines.empty?
      key = lines.first.sub("{","").strip.to_sym
      lines.delete_at 0
      blocks[key] = []
      lines.each {|elem| blocks[key] << elem.gsub("\"","").strip unless elem.strip.empty? }
    end
  end
  blocks
end

def regroup(blocks)
  regrouped = {}
  blocks.keys.each{|key| regrouped[key] = nil}                        # just to keep the ordering
  regrouped[:groups] = []
  number_of_groups = blocks[:ICD_groups_labels].length
  number_of_groups.times { regrouped[:groups] << {} }
  blocks.each do |key, elems|
    if /_groups_labels/i =~ key
      elems.each_with_index do |elem, i|
        regrouped[:groups][i][:label] = elem.gsub(/^\d*\ ?/, "")
        regrouped[:groups][i][:ICD10] = regrouped[:groups][i][:ICD10] # just to keep the ordering
        regrouped[:groups][i][:male] = elem[0] == "1"
        regrouped[:groups][i][:female] = elem[1] == "1"
        regrouped[:groups][i][:both] = elem[2] == "1"
      end
    elsif key =~/icd.*_groups/i
      elems.each_with_index do |elem, i|
        regrouped[:groups][i][:ICD10] = elem
      end
    elsif key =~ /table_description/
      regrouped[key] = elems.join("\n")
    else
      if elems.length > 1
        regrouped[key] = elems
      else
        regrouped[key] = elems.first
      end
    end
  end
  regrouped.delete_if {|k,v| v.nil?} # remove empty keys
  regrouped
end

def write_json(outfile)

end

infiles = [
  "../../conf/tables/ASR Barchart for Top Cancers.conf",
  "../../conf/tables/AgeSpecificRatesLinear.conf"
]

infiles.each {|infile| puts JSON.pretty_generate(regroup(load_conf(infile))) }