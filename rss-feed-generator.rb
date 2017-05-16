require "rss"

changelog_file = "changelog.txt"
link = "http://www.iacr.com.fr/CanReg5"
changelog = []

mode = :start
version = nil
text = []
File.read(changelog_file).each_line do |line|
  if line=~/(\d\.\d\d\.\d\d)/
    changelog << { 
      title: version, 
      summary: text.join("\n"),
      link: link,

      } unless version.nil?
    mode = :read
    version = $1
  else
    if mode == :start
      # skip
    else
      text << line
    end
  end
end
rss = RSS::Maker.make("atom") do |maker|
  maker.channel.author = "Morten Ervik"
  maker.channel.updated = Time.now.to_s
  maker.channel.about = "http://www.iacr.com.fr/CanReg5/feeds/news.rss"
  maker.channel.title = "CanReg5 Updates"

  changelog.each do |p|
      maker.items.new_item do |item|
          item.link    = p[:link]
          item.title   = p[:title]
          # item.updated = p.edited
          # item.pubDate = p.date
          item.summary = p[:summary]
          # item.content.content = 'text to set as content'
      end
  end
end

puts rss