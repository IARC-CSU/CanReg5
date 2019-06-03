generate.id <- function(type.table = "character",
                        dt = data.frame,
                        id.name = "character"){
  if (type.table == "patient"){
    #PatientID: length 8. E.g. @H000001
    #PatientRecordID: length 10. E.g. @H00000101
    dt.empty <- dt[dt[,id.name] == "",]
    if(nrow(dt.empty) == 0){
      dt.empty <- dt
      dt.empty[,id.name] <- data.frame(rep("",nrow(dt), stringsAsFactors = FALSE))
    }else{NULL}
    
    aux.id.num <- str_pad(rep(1:nrow(dt.empty)), 6, pad = "0")
    dt.empty[,id.name] <- paste("@H", aux.id.num, sep = "")
    dt.empty$PATIENTRECORDID <- paste(dt.empty[,id.name], "01", sep = "")
    if(nrow(dt.empty) != nrow(dt)){
      dt <- data.frame(rbind(dt.empty, 
                             dt[dt[,id.name] != ","]),
                       stringsAsFactors = FALSE)
    }else{
      dt <- dt.empty
    }
  }else{
    if (type.table == "tumour"){
      #PatientIDTumourTable: =  PatientID: length 8. E.g. @H000001
      #TumourID: = PatientRecordID: length 10. E.g. @H00000101
      #PatientRecordIDTumourTable: = TumourID: length 10. E.g. @H00000101
      dt.empty <- dt[dt$PATIENTIDTUMOURTABLE == "",]
      aux.id.num <- str_pad(rep(1:nrow(dt.empty)), 6, pad = "0")
      dt.empty$PATIENTIDTUMOURTABLE <- paste("@H", aux.id.num, sep = "")
      dt.empty[,id.name] <- paste(dt.empty$PATIENTIDTUMOURTABLE, "01", sep = "")
      dt.empty$PATIENTRECORDIDTUMOURTABLE <- dt.empty[,id.name]
      if(nrow(dt.empty) != nrow(dt)){
        dt <- data.frame(rbind(dt.empty, 
                               dt[dt$PATIENTIDTUMOURTABLE != ","]),
                         stringsAsFactors = FALSE)
      }else{
        dt <- dt.empty
        }
      }else{
      #TumourIDSourceTable: = TumourID
      #SourceRecordID: length 12. E.g. @H0000010101
      dt.empty <- dt[dt$TUMOURIDSOURCETABLE == "",]
      aux.id.num <- str_pad(rep(1:nrow(dt.empty)), 6, pad = "0")
      dt.empty$TUMOURIDSOURCETABLE <- paste("@H", aux.id.num, "01", sep = "")
      dt.empty$SOURCERECORDID <- paste(dt.empty$TUMOURIDSOURCETABLE, "01", sep = "")
      if(nrow(dt.empty) != nrow(dt)){
        dt <- data.frame(rbind(dt.empty, 
                               dt[dt$TUMOURIDSOURCETABLE != ","]),
                         stringsAsFactors = FALSE)
      }else{
        dt <- dt.empty
        }
      }
  }
    return(dt)
}