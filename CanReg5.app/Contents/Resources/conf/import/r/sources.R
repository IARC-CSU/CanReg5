sources <- function(source.data = data.frame,
                    non.MP.data = data.frame,
                    MP.data = data.frame,
                    source.number = integer,
                    s.var.user = array,#the array with the name of the variables for each source
                    s.var.table = array, #the array with the name of the variables for each source, they should match the order of s.var.user
                    mp.seq = "character",#multiple primary sequence
                    reg.number = "character"){#case number
  for (i in 1:source.number){
    ind.start = match (as.character(i),s.var.user)
    if (i<source.number){
      ind.end = match (as.character(i+1),s.var.user) - 1
    }else{
      ind.end = length(s.var.user)
    }
    aux.non.MP.data <- non.MP.data[,names(non.MP.data) %in% c(s.var.user[ind.start:ind.end],reg.number,mp.seq)]
    aux.non.MP.data <- aux.non.MP.data[,order(names(aux.non.MP.data))]
    aux.MP.data <- MP.data[,names(MP.data) %in% c(s.var.user[ind.start:ind.end],reg.number,mp.seq)]
    aux.MP.data <- aux.MP.data[,order(names(aux.MP.data))]
    if (nrow(aux.MP.data)>0){
      aux.MP.data$TumourIDSourceTable <- paste(aux.MP.data[,c(reg.number)],"01","0",as.character(aux.MP.data[,c(mp.seq)]),sep = "")
    }else{NULL}
    if (nrow(aux.non.MP.data)>0){
      aux.non.MP.data$TumourIDSourceTable <- paste(aux.non.MP.data[,c(reg.number)],"01","0",as.character(aux.non.MP.data[,c(mp.seq)]),sep = "")
    }else{NULL}
    source.variables <- sort(c(s.var.table[(ind.start+1):ind.end],reg.number,mp.seq,"TumourIDSourceTable"))
    aux.non.MP.data <- aux.non.MP.data[,order(names(aux.non.MP.data))]
    aux.MP.data <- aux.MP.data[,order(names(aux.MP.data))]
    if (nrow(aux.MP.data)>0){
      names(aux.MP.data) <- source.variables
      aux.MP.data$SourceRecordID <- paste(aux.MP.data$TumourIDSourceTable,"0",as.character(i),sep = "")
    }else{NULL}
    if (nrow(aux.non.MP.data)>0){
      names(aux.non.MP.data) <- source.variables
      aux.non.MP.data$SourceRecordID <- paste(aux.non.MP.data$TumourIDSourceTable,"0",as.character(i),sep = "")
    }else{NULL}
    source.data <- data.frame(rbind.fill(source.data,
                                         aux.MP.data[,names(aux.MP.data) %in% names(source.data)],
                                         aux.non.MP.data[,names(aux.non.MP.data) %in% names(source.data)]),
                              stringsAsFactors = FALSE)
    for (i in 1:ncol(source.data)){
      source.data[,i][is.na(source.data[,i])]<-""
    }
    
    #source.data$SourceRecordID <- paste(source.data$TumourIDSourceTable,"0",as.character(i),sep = "")
  }
  return(source.data[,!names(source.data)%in%c(reg.number,mp.seq)])
}