deleting.C.fn <- function(doc.data,
                          tumour.import.data){
  var.name <- toupper(doc.data$short_name[doc.data$standard_variable_name %in% "Topography"])
  tumour.import.data[, var.name] <- gsub("^[c|C]", "", tumour.import.data[,var.name])
  return(tumour.import.data)
}