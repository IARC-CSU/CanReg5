generate.id <- function(type.table = "character",
                        dt = data.frame){
  if (type.table == "patient"){
    #PatientID: length 8. E.g. @H000001
    #PatientRecordID: length 10. E.g. @H00000101
    dt.empty <- dt[dt$PatientID == "",]
    aux.id.num <- str_pad(rep(1:nrow(dt.empty)), 6, pad = "0")
    dt.empty$PatientID <- paste("@H", aux.id.num, sep = "")
    dt.empty$PatientRecordID <- paste(dt.empty$PatientID, "01", sep = "")
    if(nrow(dt.empty) != nrow(dt)){
      dt <- data.frame(rbind(dt.empty, 
                             dt[dt$PatientID != ","]),
                       stringsAsFactors = FALSE)
    }else{
      dt <- dt.empty
    }
  }else{
    if (type.table == "tumour"){
      #PatientIDTumourTable: =  PatientID: length 8. E.g. @H000001
      #TumourID: = PatientRecordID: length 10. E.g. @H00000101
      #PatientRecordIDTumourTable: = TumourID: length 10. E.g. @H00000101
      dt.empty <- dt[dt$PatientIDTumourTable == "",]
      aux.id.num <- str_pad(rep(1:nrow(dt.empty)), 6, pad = "0")
      dt.empty$PatientIDTumourTable <- paste("@H", aux.id.num, sep = "")
      dt.empty$TumourID <- paste(dt.empty$PatientIDTumourTable, "01", sep = "")
      dt.empty$PatientRecordIDTumourTable <- dt.empty$TumourID
      if(nrow(dt.empty) != nrow(dt)){
        dt <- data.frame(rbind(dt.empty, 
                               dt[dt$PatientIDTumourTable != ","]),
                         stringsAsFactors = FALSE)
      }else{
        dt <- dt.empty
        }
      }else{
      #TumourIDSourceTable: = TumourID
      #SourceRecordID: length 12. E.g. @H0000010101
      dt.empty <- dt[dt$TumourIDSourceTable == "",]
      aux.id.num <- str_pad(rep(1:nrow(dt.empty)), 6, pad = "0")
      dt.empty$TumourIDSourceTable <- paste("@H", aux.id.num, "01", sep = "")
      dt.empty$SourceRecordID <- paste(dt.empty$TumourIDSourceTable, "01", sep = "")
      if(nrow(dt.empty) != nrow(dt)){
        dt <- data.frame(rbind(dt.empty, 
                               dt[dt$TumourIDSourceTable != ","]),
                         stringsAsFactors = FALSE)
      }else{
        dt <- dt.empty
        }
      }
  }
    return(dt)
}