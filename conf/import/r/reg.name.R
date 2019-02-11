reg.name <- function(doc){
  tryCatch({
    registry.name <- xmlToDataFrame(nodes = (xmlChildren(xmlRoot(doc)[["general"]])),
                                    stringsAsFactors = FALSE)$text[3]
    return(registry.name)
  }, error = function(er) {
    registry.name <- "No registry name"
    print(er)
  })
}