var.length <- function (doc.data = data.frame,
                        dic.data = data.frame){
  #Complete variable_length in the doc.data so we could put leading zeros in the variables that use dictionary data
  doc.data <- doc.data[order(doc.data$use_dictionary), ]
  dic.data <- dic.data[order(dic.data$name), ]
  for (i in 1:nrow(dic.data)){
    doc.data[doc.data$use_dictionary %in% dic.data$name[i],"variable_length"] <- as.numeric(dic.data$full_dictionary_code_length[i])
  }
  
  doc.data$variable_length <- as.numeric(doc.data$variable_length)
  return(doc.data)
}