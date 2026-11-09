get.var <- function(doc.data = data.frame,
                    variable = "character"){
  if (variable == "update"){
    variable.update <- doc.data$short_name[doc.data$english_name=="Update"]
    return (variable.update)
  }else{
    if (variable == "pmtot"){
      variable.pmtot <- doc.data$short_name[doc.data$english_name=="M.P.Tot."]
      return (variable.pmtot)
    }else{
      if (variable == "regnumber"){
        variable.regnumber <- doc.data$short_name[doc.data$english_name=="Reg.No."]
        return (variable.regnumber)
      }else{
        if (variable == "pmseq"){
          variable.pmseq <- doc.data$short_name[doc.data$english_name=="M.P.Seq."]
          return (variable.pmseq)
        }else{
          variable.mp <- doc.data$short_name[doc.data$english_name=="M.P.code"]
          return(variable.mp)
        }
       
      }
    }
  }
  
}