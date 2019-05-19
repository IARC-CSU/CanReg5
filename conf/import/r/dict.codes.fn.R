dict.codes.fn <- function(dic.codes){
  df.codes <- grep("(^[[:punct:]][[:digit:]])", dic.codes$V1, perl = TRUE, value = TRUE)
  df.dic.names <- grep("(^[[:punct:]]{4}[[:alpha:]])", dic.codes$V2, perl = TRUE, value = TRUE)
  dic_idx <- which(dic.codes$V1 %in% df.codes)
  dic_idx <- c(dic_idx, nrow(dic.codes))
  dic.codes.tidy <- data.frame(matrix(ncol = 4, nrow = 0))
  for (i in 1:(length(dic_idx)-1)){
    idx_ini <- dic_idx[i]
    idx_end <- dic_idx[i+1]
    if (idx_end - idx_ini >1){
      if(i != length(df.codes)){
        codes.df <- rep(df.codes[i],(idx_end - idx_ini -1))
        dic.names.df <- rep(df.dic.names[i],(idx_end - idx_ini -1))
        aux.df <- cbind(dic.codes[(idx_ini + 1):(idx_end - 1),],codes.df, dic.names.df)
      }else{
        dic.names.df <- rep(df.dic.names[i],(idx_end - idx_ini))
        codes.df <- rep(df.codes[i],(idx_end - idx_ini))
        aux.df <- cbind(dic.codes[(idx_ini + 1):idx_end,],codes.df, dic.names.df)
      }
      
      dic.codes.tidy <- rbind(dic.codes.tidy, aux.df)
    }else{NULL}
    
    
  }
  colnames(dic.codes.tidy) <- c("code", "description", "dictionary_id","name_dictionary")
  
  dic.codes.tidy$dictionary_id <- str_replace(dic.codes.tidy$dictionary_id, "\\#", "")
  dic.codes.tidy$name_dictionary <- str_replace(dic.codes.tidy$name_dictionary, "\\----", "")
  dic.codes.tidy <- merge(dic.codes.tidy,dic.data,by = c("dictionary_id"))
  dic.codes.tidy <- dic.codes.tidy[, c("code", "description", "dictionary_id", "name_dictionary", "full_dictionary_code_length")]
  return(dic.codes.tidy)
}