length.col <- function(source.dic=character(),
                       doc.data=data.frame){
  
  for (i in 1:length(source.dic)){
    source.data[,source.dic[i]] <- formatC(source.data[,source.dic[i]],
                                           width = doc.data$variable_length[doc.data$short_name==source.dic[i]],
                                           flag="0")
  }
}