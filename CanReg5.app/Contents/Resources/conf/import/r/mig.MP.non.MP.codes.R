mig.MP.non.MP.codes <- function(MP.data = data.frame,
                                non.MP.data = data.frame,
                                MPCod = "character"){
  #Create a table with the frequencies of multiple primary code
  #The codes that have just frequency 1 are going to be move to non.MP.data
  
  MPCod.data <- as.data.frame(table(MP.data[,c(MPCod)]), row.names = NULL,
                              responseName = "Freq", stringsAsFactors = FALSE)
    
  MPCod.array <- array()
  if (nrow(MPCod.data)>0){
    for (i in 1:nrow(MPCod.data)){
      if (MPCod.data$Freq[i]<2){
        MPCod.array <- append (MPCod.array,MPCod.data$Var1[i])
      }else{NULL}
    }
    if (length(MPCod.array)>1){
      MPCod.array <- MPCod.array[!is.na(MPCod.array)]
      return(MPCod.array)
    }else{
      return("FALSE")
    }
    
  }else{
    return("FALSE")
  }
  
}