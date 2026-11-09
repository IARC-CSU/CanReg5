mp.error <- function (MP.data = data.frame,
                      update.date = "character",
                      p.var.user = array,
                      mp.cod = "character"){
  PMCod.data <- MP.data$PMCod[!duplicated(MP.data$PMCod)]
  error.MP <- array()
  for (i in 1:length(PMCod.data)){
    aux.PM <- MP.data[MP.data[,c(mp.cod)]==PMCod.data[i],]
    #The most updated patient is going to be compared with the rest of the cases.
    #Note: the user has to determine the variable for update date
    aux.PM <- aux.PM[order(aux.PM[,c(update.date)]),]
    aux.PM[is.na(aux.PM)] <- ""
    for (j in 2:nrow(aux.PM)){
      if (nrow(aux.PM)>1){
        if (!all((aux.PM[1,p.var.user]==aux.PM[j,p.var.user]),TRUE)){
          error.MP <- append(error.MP, PMCod.data[i], after = (i-1))
        }else{NULL}
      }else{NULL}
    }
  }
  #Create a data.frame that contains the cases that have differences
  #This data.frame is going to be shown to the user
  if (length(error.MP)>1){
    error.MP <- error.MP[!is.na(error.MP)]
    error.MP.cases <- MP.data[MP.data[,c(mp.cod)] %in% error.MP,]
    return(error.MP.cases)
  }else{
    return("There is not any error with the Patients")
  }
  
}