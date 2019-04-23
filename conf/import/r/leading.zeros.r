leading.zeros <- function(table.data,
                          doc.data,
                          type.table = "character"){
  doc.data$short_name <- toupper(doc.data$short_name)
  doc.data$standard_variable_name <- toupper(doc.data$standard_variable_name)
  dic.data <- doc.data$short_name[doc.data$table == type.table & doc.data$variable_type == "Dict"]
  for (i in 1:length(dic.data)){
    #table.data[,dic.data[i]] <- as.integer(table.data[,dic.data[i]])
    table.data[is.na(table.data[,dic.data[i]]) | table.data[,dic.data[i]] =="",c(dic.data[i])] <- -1
    
    table.data[,dic.data[i]] <- formatC(as.integer(table.data[,dic.data[i]]),
                                        width = doc.data$variable_length[doc.data$short_name==dic.data[i]],
                                        flag="0")
    aux <- formatC(-1,
                   width = doc.data$variable_length[doc.data$short_name==dic.data[i]],
                   flag="0")
    table.data[table.data[,dic.data[i]]==aux,dic.data[i]] <- ""
  }
  
  return(table.data)
}