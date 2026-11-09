tables.cr5 <- function (doc.data = data.frame,
                        table.cr5 = "character"){#Source, Tumour or Patient
  #We will create a dataframe for patient, tumour and source
  tables.cr5.names <- t(doc.data$short_name[doc.data$table == table.cr5])
  tables.cr5.data <- data.frame(matrix(vector(), nrow = 0, ncol = length(tables.cr5.names)),
                                stringsAsFactors=F)
  names(tables.cr5.data) <- tables.cr5.names

  return(tables.cr5.data)
}
