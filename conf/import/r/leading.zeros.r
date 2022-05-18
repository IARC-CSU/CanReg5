leading.zeros <- function(table.data = data.frame,
                          doc.data,
                          type.table = "character"){
  doc.data$short_name <- toupper(doc.data$short_name)
  doc.data$standard_variable_name <- toupper(doc.data$standard_variable_name)
  dic.data <- doc.data$short_name[doc.data$table == type.table & doc.data$variable_type == "Dict"]
  for (i in 1:length(dic.data)){
    #table.data[,dic.data[i]] <- as.integer(table.data[,dic.data[i]])
    if (!(all(table.data[,dic.data[i]] == "") | all(is.na(table.data[,dic.data[i]])))){
      aux.table.data <- table.data[, dic.data[i]]
      table.data[is.na(table.data[,dic.data[i]]) | table.data[,dic.data[i]] == "",c(dic.data[i])] <- -1
      
      table.data[,dic.data[i]] <- formatC(as.integer(table.data[,dic.data[i]]),
                                          width = doc.data$variable_length[doc.data$short_name == dic.data[i]],
                                          flag = "0")
      aux <- formatC(-1,
                     width = doc.data$variable_length[doc.data$short_name == dic.data[i]],
                     flag = "0")
      table.data[table.data[,dic.data[i]] == aux,dic.data[i]] <- ""
      table.data <- as.data.table(table.data)
      dic.data.i <- dic.data[i]
      table.data[, dic.data[i] := ifelse(table.data[,get(dic.data[i])] %like% "*NA", aux.table.data, table.data[,get(dic.data[i])])]
      table.data <- as.data.frame(table.data, stringsAsFactors = FALSE)
    }else{NULL}
    
  }
  table.data <- as.data.frame(table.data, stringsAsFactors = FALSE)
  return(table.data)
}