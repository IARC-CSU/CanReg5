reg.language <- function(doc){
  registry.language <- xmlToDataFrame(nodes = (xmlChildren(xmlRoot(doc)[["general"]])),
                                    stringsAsFactors = FALSE)$text[4]
  return(registry.language)
}