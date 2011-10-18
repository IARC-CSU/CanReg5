look_for = "program.VERSION="
inno_outfile = File.new("inno-settings.txt", "w+")
version_outfile = File.new("version.txt", "w+")
File.open("appinfo.properties").each do |line|
  unless line.start_with? '#'    
    if line.start_with? look_for
      # puts line
      version = line.sub(look_for,'').strip
      # puts version
      inno_outfile.puts '#define MyAppVersion "'.concat(version).concat('"')
      version_outfile.puts version
    end
  end
end
