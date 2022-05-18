check.save <- function(total.patient= as.integer,
                       MP.data = data.frame,
                       non.MP.data = data.frame,
                       patient.data = data.frame,
                       tumour.data = data.frame,
                       source.data = data.frame,
                       folder = "character"){
  if (total.patients==nrow(patient.data) & (nrow(MP.data)+nrow(non.MP.data))==nrow(tumour.data) & nrow(source.data)>0){
      #Write the csv files that are going to be imported
      write.csv(tumour.data, file = paste(folder,"/","tumour.csv",sep = ""), row.names = FALSE)
      write.csv(patient.data, file = paste(folder,"/","patient.csv",sep = ""), row.names = FALSE)
      write.csv(source.data, file = paste(folder,"/","source.csv",sep = ""), row.names = FALSE)
      return (TRUE)
    }else{
      print (FALSE)
    }
}
