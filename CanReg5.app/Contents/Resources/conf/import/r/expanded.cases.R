expanded.cases <- function(MP.data = data.frame,
                           mp.cod = "character",#multiple primary code
                           mp.total = "character",#total number of tumours
                           reg.number = "character"){#case number
  #Create a dataframe with 2 columns (Case and PMCod), deleting the duplicates, we are going to
  #have just one patient per case. The Case number is going to be repeated as many times as the
  #variable MPTot says
  #We should ask for the variable that is used to save the code for MP, the
  #variable that contains the total number of tumours that the patient has and
  #the variable that has the case number
  #Order the database by PMCod
  
  MP.data <- MP.data[order(MP.data[,c(mp.cod)]),]
  ec.data <- expandRows(MP.data[!duplicated(MP.data[,c(mp.cod)]),
                                c(reg.number,mp.total)],mp.total)
  return (ec.data)
  
}