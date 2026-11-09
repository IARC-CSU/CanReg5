tumour <- function (tumour.data = data.frame,
                    non.MP.data = data.frame,
                    MP.data = data.frame,
                    mp.seq = "character",#multiple primary sequence
                    reg.number = "character"){#case number
  if (nrow(MP.data)>0){
    MP.data$PatientRecordIDTumourTable <- paste(MP.data[,c(reg.number)],"01",sep = "")
    MP.data$PatientIDTumourTable <- MP.data[,c(reg.number)]
    MP.data$TumourID <- paste(MP.data$PatientRecordIDTumourTable,"0",as.character(MP.data[,c(mp.seq)]),sep = "")
  }else{NULL}
  if (nrow(non.MP.data)>0){
    non.MP.data$PatientRecordIDTumourTable <- paste(non.MP.data[,c(reg.number)],"01",sep = "")
    non.MP.data$PatientIDTumourTable <- non.MP.data[,c(reg.number)]
    non.MP.data$TumourID <- paste(non.MP.data$PatientRecordIDTumourTable,"0",as.character(non.MP.data[,c(mp.seq)]),sep = "")
  }else{NULL}
  MP.data[is.na(MP.data)] <- ""
  non.MP.data[is.na(non.MP.data)] <- ""
  
  tumour.data <- data.frame(rbind.fill(tumour.data, 
                                       non.MP.data[,names(non.MP.data) %in% names(tumour.data)],
                                       MP.data[,names(MP.data) %in% names(tumour.data)]),
                            stringsAsFactors = FALSE)
  tumour.data[is.na(tumour.data)] <- ""
  tumour.data$ObsoleteFlagTumourTable <- 0
  tumour.data$TumourUnduplicationStatus <- 0
  return(tumour.data)
}