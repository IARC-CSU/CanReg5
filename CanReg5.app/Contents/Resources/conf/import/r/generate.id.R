generate.id <- function(type.table = "character",
                        dt = data.frame,
                        id.name = "character",
                        mp.seq = "character"){
  if (type.table == "patient"){
    #PatientID: length 8. E.g. @H000001
    #PatientRecordID: length 10. E.g. @H00000101
    dt.empty <- as.data.frame(dt[dt[,id.name] == "",], stringsAsFactors = FALSE)
    if(nrow(dt.empty) == 0){
      dt.empty <- dt
      dt.empty$original.id <- dt.empty[,id.name]
      dt.empty[,id.name] <- ""
    }else{NULL}
    
    #To generate id for MP cases
    aux.id <- as.data.frame(table(dt[,id.name]), stringsAsFactors = FALSE)
    if(any(aux.id$Freq > 1)){
      aux.id.MP <- aux.id$Var1[aux.id$Freq > 1]
      dt.empty.MP <- dt.empty[dt.empty$original.id %in% aux.id.MP,]
      dt.empty.non.MP <- dt.empty[!(dt.empty$original.id %in% aux.id.MP),]
      for (i in 1:length(aux.id.MP)){
        aux.id.num <- str_pad(rep(1:length(aux.id.MP)), 6, pad = "0")
        dt.empty.MP[dt.empty.MP$original.id == aux.id.MP[i],id.name] <- paste("@H", aux.id.num[i], sep = "")
      }
      dt.empty.MP$PATIENTRECORDID <- paste(dt.empty.MP[,id.name], "01" , sep = "")
      aux.id.num <- str_pad(rep((length(aux.id.num)+1):(nrow(dt.empty.non.MP)+length(aux.id.num))), 6, pad = "0")
      dt.empty.non.MP[,id.name] <- paste("@H", aux.id.num, sep = "")
      dt.empty.non.MP$PATIENTRECORDID <- paste(dt.empty.non.MP[,id.name], "01", sep = "")
      dt.empty <- data.frame(rbind(dt.empty.MP, dt.empty.non.MP), stringsAsFactors = FALSE)
    }else{
      aux.id.num <- str_pad(rep(1:nrow(dt.empty)), 6, pad = "0")
      dt.empty[,id.name] <- paste("@H", aux.id.num, sep = "")
      dt.empty$PATIENTRECORDID <- paste(dt.empty[,id.name], "01", sep = "")
    }
   
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
      dt.empty <- data.frame(dt[dt$PATIENTIDTUMOURTABLE == "",], stringsAsFactors = FALSE)
      if(nrow(dt.empty) == 0){
        dt.empty <- dt
        dt.empty$original.tumour.id <- dt.empty[,id.name]
        dt.empty[,id.name] <- ""
      }else{NULL}
      
      #To generate id for MP cases
      aux.id <- as.data.frame(table(dt[,id.name]), stringsAsFactors = FALSE)
      if(any(aux.id$Freq > 1)){
        aux.id.MP <- aux.id$Var1[aux.id$Freq > 1]
        dt.empty.MP <- dt.empty[dt.empty$original.tumour.id %in% aux.id.MP,]
        dt.empty.non.MP <- dt.empty[!(dt.empty$original.tumour.id %in% aux.id.MP),]
        for (i in 1:length(aux.id.MP)){
          aux.id.num <- str_pad(rep(1:length(aux.id.MP)), 6, pad = "0")
          dt.empty.MP[dt.empty.MP$original.tumour.id == aux.id.MP[i],id.name] <- paste("@H", aux.id.num[i], sep = "")
        }
        dt.empty.MP$PATIENTRECORDIDTUMOURTABLE <- paste(dt.empty.MP[,id.name], "01", sep = "")
        aux.id.num <- str_pad(rep((length(aux.id.num)+1):(nrow(dt.empty.non.MP)+length(aux.id.num))), 6, pad = "0")
        dt.empty.non.MP[,id.name] <- paste("@H", aux.id.num, sep = "")
        dt.empty.non.MP$PATIENTRECORDIDTUMOURTABLE <- paste(dt.empty.non.MP[,id.name], "01", sep = "")
        dt.empty <- data.frame(rbind(dt.empty.MP, dt.empty.non.MP), stringsAsFactors = FALSE)
        dt.empty$TUMOURID <- paste(dt.empty$PATIENTRECORDIDTUMOURTABLE, "0", dt.empty[,mp.seq], sep = "")
      }else{
        aux.id.num <- str_pad(rep(1:nrow(dt.empty)), 6, pad = "0")
        dt.empty[,id.name] <- paste("@H", aux.id.num, sep = "")
        dt.empty$PATIENTRECORDIDTUMOURTABLE<- paste("@H", aux.id.num, "01", sep = "")
        dt.empty$TUMOURID <- paste(dt.empty$PATIENTRECORDIDTUMOURTABLE, "0", dt.empty[,mp.seq], sep = "")
      }
      
      if(nrow(dt.empty) != nrow(dt)){
        dt <- data.frame(rbind(dt.empty, 
                               dt[dt$PATIENTIDTUMOURTABLE != ","]),
                         stringsAsFactors = FALSE)
      }else{
        dt <- dt.empty
        }
      }else{
      #TumourIDSourceTable: = TumourID
      #SourceRecordID: length 14. E.g. @H0000010101
        
        
        
        
        
      dt.empty <- as.data.frame(dt[dt$TUMOURIDSOURCETABLE == "",], stringsAsFactor = FALSE)
      if(nrow(dt.empty) == 0){
        dt.empty <- dt
        dt.empty[,id.name] <- ""
      }else{NULL}
      
      aux.id.num <- str_pad(rep(1:nrow(dt.empty)), 6, pad = "0")
      dt.empty$TUMOURIDSOURCETABLE <- paste("@H", aux.id.num, "0", dt.empty[,mp.seq], sep = "")
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