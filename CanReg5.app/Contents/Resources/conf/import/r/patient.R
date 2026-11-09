patient <- function (patient.data = data.frame,
                     non.MP.data = data.frame,
                     MP.data = data.frame,
                     update.date = "character",
                     mp.cod = "character",#multiple primary code
                     reg.number = "character"){#case number
  non.MP.data$PatientUpdateDate <- non.MP.data[,c(update.date)]
  MP.data$PatientUpdateDate <- MP.data[,c(update.date)]
  patient.data<-data.frame(rbind.fill(patient.data,
                                      non.MP.data[,names(MP.data) %in% names(patient.data)],
                                      MP.data[!duplicated(MP.data[,c(reg.number)]),names(MP.data) %in% names(patient.data)]),
                           stringsAsFactors = FALSE)
  patient.data$PatientRecordID <- paste(patient.data[,c(reg.number)],"01",sep="")
  patient.data$PatientCheckStatus <- 0
  patient.data[is.na(patient.data)] <- ""
  return(patient.data)
  
}