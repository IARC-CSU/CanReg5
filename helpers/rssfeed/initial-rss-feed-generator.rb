require "rss"
require "csv"

changelog_file    = "../../changelog.txt"
twitter_feed_file = "./tweets.csv"
link = "http://www.iacr.com.fr/CanReg5"
rss_file = "rss.xml"
changelog = []

versionToDateMap = {}

CSV.open(twitter_feed_file, headers: true ).each do |line|
  version = nil
  if /CanReg5 version (5\.\d\d.\d\d)/=~line["text"]
    version = $1    
  elsif /CanReg5 update (\d\d)/=~line["text"]
    version = "5.00.#{$1}"
  end
  unless version.nil?
    date = Time.parse(line["timestamp"])
    versionToDateMap[version] = date
  end
end
puts versionToDateMap
# exit

mode = :start
version = nil
text = []
File.read(changelog_file).each_line do |line|
  if line=~/(\d\.\d\d\.\d\d)/
    changelog << { 
      version: version, 
      summary: text.join("\n"),
      link: link,
      } unless version.nil?
    mode = :read
    version = $1
    text = []
  else
    if mode == :start
      # skip
    else
      text << line
    end
  end
end

changelog.sort_by! {|v| v[:version]}

rss = RSS::Maker.make("atom") do |maker|
  maker.channel.author = "Morten Ervik"
  maker.channel.updated = Time.now.to_s
  maker.channel.about = "http://www.iacr.com.fr/CanReg5/rss.xml"
  maker.channel.title = "CanReg5 Updates"

  changelog.each do |p|
    unless versionToDateMap[p[:version]].nil?
      maker.items.new_item do |item|
          item.link    = p[:link]
          item.title   = p[:version]
          item.updated = versionToDateMap[p[:version]].to_s
          item.pubDate = versionToDateMap[p[:version]].to_s
          item.summary = p[:summary]
          # item.content.content = 'text to set as content'
      end
    end
  end
end

puts rss

File.open(rss_file, "w") << rss

