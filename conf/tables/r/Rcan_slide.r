  ## To get the R folder of the actual script
  initial.options <- commandArgs(trailingOnly = FALSE)
  file.arg.name <- "--file="
  script.name <- sub(file.arg.name, "", 
                     initial.options[grep(file.arg.name, initial.options)])
  script.basename <- dirname(script.name)
  
  ## Load Rcan function
  source(paste(sep="/", script.basename, "Rcan_core.r"))
  
  ## to get canreg argument list
  Args <- commandArgs(TRUE)
  ls_args <- canreg_args(Args)
  
  
tryCatch({
  
  #load dependency packages
  canreg_load_packages(c("Rcpp", "data.table", "ggplot2", "gridExtra", "scales", "Cairo","grid","officer","flextable", "zip", "bmp", "jpeg", "png"), Rcan_source=script.basename)
  
  #merge incidence and population
  dt_all <- csu_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  
  graph_width <- 8
  graph_width_split <- 4
  graph_width_vertical <- 5
  time_limit <- 9
  nb_slide <- 1
  
  year_info <- canreg_get_years(dt_all)
  
  ## get age group label
  canreg_age_group <- canreg_get_agegroup_label(dt_all, ls_args$agegroup)
  
  ## get age group for cumulative risk 
  temp <- min(as.numeric(substr(ls_args$agegroup,regexpr("-", ls_args$agegroup)[1]+1,nchar(ls_args$agegroup))),14)
  canreg_age_group_cr <- canreg_get_agegroup_label(dt_all, paste0("0-",temp))
  
  doc <- read_pptx(path=paste(sep="/", script.basename,"slide_template", "canreg_template.pptx"))
  
  #slide.layouts(doc)
  
  doc <-  add_slide(doc, layout="Canreg_title", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "ctrTitle", str = ls_args$header)
  
  date <- format(Sys.time(), "%B/%Y")
  date <- paste0(toupper(substr(date,1,1)),substr(date,2,nchar(date)))
  doc <- ph_with_text(doc, type = "subTitle", str = date)
  
  #################
  doc <-  add_slide(doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Population pyramid")
  
  dt_report <- dt_all
  dt_report <- canreg_pop_data(dt_report)
  
  canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph", nb_slide),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_population_pyramid,
                df_data =dt_report,
                canreg_header = "")
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, ".png")), "dim" )
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2])
  
  nb_slide <- nb_slide +1
  
  
  ################# 
  doc <-  add_slide(doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Number of cases by age group & sex")
  
  # bar chart age
  dt_report <- dt_all
  dt_report[ICD10GROUP != "C44",]$ICD10GROUP ="O&U" 
  dt_report[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified" 
  dt_report <- dt_report[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
  dt_report <- canreg_age_cases_data(dt_report,age_group = c(5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80), skin=TRUE)
  
  canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph", nb_slide),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_cases_age_bar,
                df_data =dt_report,
                canreg_header = "", skin=FALSE)
  
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, ".png")), "dim" )
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2])
  nb_slide <- nb_slide +1
  
  
  ################# 
  doc <-  add_slide(doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Proportion of cases by age group & sex")
  
  dt_report <- dt_all
  dt_report[ICD10GROUP != "C44",]$ICD10GROUP ="O&U" 
  dt_report[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified" 
  dt_report <- dt_report[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
  dt_report <- canreg_age_cases_data(dt_all, skin=TRUE)
  
  
  canreg_output(output_type = "png", filename = paste0(tempdir(),"\\temp_graph", nb_slide),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_age_cases_pie_multi_plot,
                dt=dt_report,
                canreg_header = "All cancers but C44")
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, ".png")), "dim" )
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2])
  nb_slide <- nb_slide +1
  
  ################# 
  doc <-  add_slide(doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Number of cases by year")
  
  
  dt_report <- dt_all
  dt_report[ICD10GROUP != "C44",]$ICD10GROUP ="O&U" 
  dt_report[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified" 
  dt_report <- dt_report[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
  
  dt_report <- canreg_year_cases_data(dt_report, skin=FALSE)
  
  
  ##Produce output
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", nb_slide),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_cases_year_bar,
                dt=dt_report,
                canreg_header = "", skin=FALSE)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, ".png")), "dim" )
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, ".png"),width=graph_width,height=graph_width*dims[1]/dims[2])
  nb_slide <- nb_slide +1
  
  
  ################# 
  
  doc <-  add_slide(doc, layout="Canreg_basic_subtitle", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Top 10 cancer, both sexes")
  doc <- ph_with_text(doc, type = "body", str = "Number of cases")
  
  dt_report <- dt_all
  dt_report <- dt_report[ICD10GROUP != "C44",]
  dt_report <- dt_report[ICD10GROUP != "O&U",]
  
  dt_report <- canreg_ageSpecific_rate_data(dt_report, keep_ref = TRUE)
  
  
  
  
  dt_report<- Rcan:::core.csu_asr(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                  group_by = c("cancer_label", "SEX"),
                                  missing_age = canreg_missing_age(dt_report),
                                  first_age = canreg_age_group$first_age+1,
                                  last_age= canreg_age_group$last_age+1,
                                  pop_base_count = "REFERENCE_COUNT",
                                  age_label_list = "AGE_GROUP_LABEL")
  
  
  canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph", nb_slide),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_bar_top,
                df_data=dt_report,
                var_top="CASES",
                nsmall=0,
                color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),nb_top = 10,
                canreg_header = "",
                ytitle=paste0("Number of cases, ", canreg_age_group$label))
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, ".png")), "dim" )
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, ".png"), index=4, width=graph_width,height=graph_width*dims[1]/dims[2])
  nb_slide <- nb_slide +1
  
  
  
  
  ################# 
  doc <-  add_slide(doc, layout="Canreg_basic_subtitle", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Top 10 cancer, both sexes")
  doc <- ph_with_text(doc, type = "body", str = "Age-standardized incidence rate")
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", nb_slide),landscape = TRUE,list_graph = FALSE,
                FUN=canreg_bar_top,
                df_data=dt_report,color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),nb_top = 10,
                canreg_header = "",
                ytitle=paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label))
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, ".png")), "dim" )
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, ".png"), index=4, width=graph_width,height=graph_width*dims[1]/dims[2])
  nb_slide <- nb_slide +1
  #################
  
  doc <-  add_slide(doc, layout="Canreg_split", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Top 10 cancer: Number of cases")
  
  dt_report <- dt_all
  dt_report <- dt_report[ICD10GROUP != "C44",]
  dt_report <- dt_report[ICD10GROUP != "O&U",]
  dt_report <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = FALSE)
  
  
  ##calcul of ASR
  dt_asr<- Rcan:::core.csu_asr(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                               group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                               first_age = canreg_age_group$first_age+1,
                               last_age= canreg_age_group$last_age+1,
                               pop_base_count = "REFERENCE_COUNT",
                               age_label_list = "AGE_GROUP_LABEL")
  
  ##calcul of cumulative risk
  dt_cum_risk <- csu_cum_risk_core(df_data = dt_report,var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                   group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                                   last_age= canreg_age_group_cr$last_age+1,
                                   age_label_list = "AGE_GROUP_LABEL")
  
  dt_bar <- dt_asr
  var_top <- "CASES"
  digit <- 0
  xtitle <- paste0("Number of cases, ", canreg_age_group$label)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", nb_slide),landscape = FALSE,list_graph = TRUE,
                FUN=canreg_bar_top_single,
                dt=dt_bar,var_top=var_top,nb_top = 10,
                canreg_header = "",digit=digit,
                xtitle=xtitle)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, "001.png")), "dim" )
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "001.png"), index=2, width=graph_width_split,height=graph_width_split*dims[1]/dims[2])
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "002.png"), index=4, width=graph_width_split,height=graph_width_split*dims[1]/dims[2])
  nb_slide <- nb_slide +1
  
  
  
  ################# 
  doc <-  add_slide(doc, layout="Canreg_split", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Top 10 cancer: ASR")
  
  
  var_top <- "asr"
  digit <- 1
  xtitle<-paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", nb_slide),landscape = FALSE,list_graph = TRUE,
                FUN=canreg_bar_top_single,
                dt=dt_bar,var_top=var_top,nb_top = 10,
                canreg_header = "",digit=digit,
                xtitle=xtitle)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, "001.png")), "dim" )
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "001.png"), index=2, width=graph_width_split,height=graph_width_split*dims[1]/dims[2])
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "002.png"), index=4, width=graph_width_split,height=graph_width_split*dims[1]/dims[2])
  nb_slide <- nb_slide +1
  ################# 
  doc <-  add_slide(doc, layout="Canreg_split", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Top 10 cancer: Cumulative risk")
  
  var_top <- "cum_risk"
  digit <- 2
  dt_bar <- dt_cum_risk
  xtitle<-paste0("Cumulative incidence risk (percent), ", canreg_age_group_cr$label)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", nb_slide),landscape = FALSE,list_graph = TRUE,
                FUN=canreg_bar_top_single,
                dt=dt_bar,var_top=var_top,nb_top = 10,
                canreg_header = "",digit=digit,
                xtitle=xtitle)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, "001.png")), "dim" )
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "001.png"), index=2, width=graph_width_split,height=graph_width_split*dims[1]/dims[2])
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "002.png"), index=4, width=graph_width_split,height=graph_width_split*dims[1]/dims[2])
  nb_slide <- nb_slide +1
  ################# ####
  
  
  
  
  
  dt_report <- dt_all
  dt_report <- dt_report[ICD10GROUP != "C44",]
  dt_report <- dt_report[ICD10GROUP != "O&U",]
  dt_report <- canreg_ageSpecific_rate_data(dt_report)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", nb_slide),landscape = FALSE,list_graph = TRUE,
                FUN=canreg_ageSpecific_rate_top,
                df_data=dt_report,logscale = TRUE,nb_top = 5,
                plot_title = "")
  
  doc <-  add_slide(doc, layout="Canreg_vertical", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Age-specific rates:\r\nMales")
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, "001.png")), "dim" )
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "001.png"), index=2, width=graph_width_vertical,height=graph_width_vertical*dims[1]/dims[2])
  
  doc <-  add_slide(doc, layout="Canreg_vertical", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Age-specific rates:\r\nFemales")
  doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "002.png"), index=2, width=graph_width_vertical,height=graph_width_vertical*dims[1]/dims[2])
  nb_slide <- nb_slide +1
  
  
  
  #################  
  if (year_info$span > time_limit) {  
    
    
    dt_report <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = TRUE)
    
    
    ## get age group label
    
    
    ##calcul of ASR
    dt_report<- Rcan:::core.csu_asr(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                    group_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                                    first_age = canreg_age_group$first_age+1,
                                    last_age= canreg_age_group$last_age+1,
                                    pop_base_count = "REFERENCE_COUNT",
                                    age_label_list = "AGE_GROUP_LABEL")
    
    
    #produce graph
    canreg_output(output_type = "png", filename = paste0(tempdir(),"\\temp_graph", nb_slide),landscape = FALSE,list_graph = TRUE,
                  FUN=canreg_asr_trend_top,
                  dt=dt_report,number = 5,
                  canreg_header = "",
                  ytitle=paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label))
    
    doc <-  add_slide(doc, layout="Canreg_vertical", master="Office Theme") ## add PPTX slide (Title + content)
    doc <- ph_with_text(doc, type = "title", str = "Trend in ASR:\r\nMales")
    
    dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, "001.png")), "dim" )
    doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "001.png"), index=2, width=graph_width_vertical,height=graph_width_vertical*dims[1]/dims[2])
    
    doc <-  add_slide(doc, layout="Canreg_vertical", master="Office Theme") ## add PPTX slide (Title + content)
    doc <- ph_with_text(doc, type = "title", str = "Trend in ASR:\r\nFemales")
    doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "002.png"), index=2, width=graph_width_vertical,height=graph_width_vertical*dims[1]/dims[2])
    nb_slide <- nb_slide +1
    
  }
  
  if (year_info$span > time_limit) {
    
    dt_report <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = TRUE)
    
    
    ##calcul of ASR
    dt_report<- Rcan:::core.csu_asr(df_data =dt_report, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                    group_by = c("cancer_label", "SEX", "YEAR"), missing_age = canreg_missing_age(dt_all),
                                    first_age = canreg_age_group$first_age+1,
                                    last_age= canreg_age_group$last_age+1,
                                    pop_base_count = "REFERENCE_COUNT",
                                    age_label_list = "AGE_GROUP_LABEL")
    
    ##Keep top based on rank
    dt_report <- Rcan:::core.csu_dt_rank(dt_report,
                                         var_value= "CASES",
                                         var_rank = "cancer_label",
                                         group_by = "SEX",
                                         number = 25
    )
    
    
    ##calcul eapc
    dt_report <- Rcan:::core.csu_eapc(dt_report, var_rate = "asr",var_year = "YEAR" ,group_by =c("cancer_label", "SEX","CSU_RANK"))
    dt_report <-as.data.table(dt_report)
    
    
    #produce graph
    canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", nb_slide),landscape = TRUE,list_graph = TRUE,
                  FUN=canreg_eapc_scatter_error_bar,
                  dt=dt_report,
                  canreg_header = "Estimated Average Percentage Change",
                  ytitle=paste0("Estimated average percentage change (%), ", canreg_age_group$label))
    
    
    dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, "001.png")), "dim" )
    
    doc <-  add_slide(doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
    doc <- ph_with_text(doc, type = "title", str = "Estimated annual percentage change:\r\nMales")
    doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "001.png"),width=graph_width,height=graph_width*dims[1]/dims[2])
    
    
    doc <-  add_slide(doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
    doc <- ph_with_text(doc, type = "title", str = "Estimated annual percentage change:\r\nFemales")
    doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, "002.png"),width=graph_width,height=graph_width*dims[1]/dims[2])
    nb_slide <- nb_slide +1
    
  }
  
  region_admit <- c("EastMed", "Americas", "West Pacific", "Europe", "SEAsia", "Africa")
  if (ls_args$sr %in% region_admit) {
    
    dt_report <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE)
    
    # import CI5 data with same cancer code and same age group
    dt_CI5_data <- canreg_import_CI5_data(dt_report, paste0(script.basename, "/CI5_data.rds"))
    
    #merge CI5 and canreg data
    dt_both <- canreg_merge_CI5_registry(dt_report,dt_CI5_data, registry_region = ls_args$sr, registry_label = ls_args$header )
    
    #create bar chart graphique 
    setkeyv(dt_both, c("CSU_RANK", "SEX","asr"))
    dt_both[country_label!=ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"6E")]
    dt_both[country_label==ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"FF")]
    
    
    canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph", nb_slide),landscape = TRUE,list_graph = TRUE,
                  FUN=canreg_bar_CI5_compare,
                  dt=dt_both,xtitle=paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=",")))
    
    dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph", nb_slide, "001.png")), "dim" )
    
    for (i in seq(1,10,2)) {
      
      doc <-  add_slide(doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
      doc <- ph_with_text(doc, type = "title", str = "Comparison of summary rates with other registries")
      doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, sprintf("%03d",i), ".png"),width=graph_width,height=graph_width*dims[1]/dims[2])
    }
    
    for (i in seq(2,10,2)) {
      
      
      doc <-  add_slide(doc, layout="Canreg_basic", master="Office Theme") ## add PPTX slide (Title + content)
      doc <- ph_with_text(doc, type = "title", str = "Comparison of summary rates with other registries")
      doc <- ph_with_img(doc,paste0(tempdir(), "\\temp_graph", nb_slide, sprintf("%03d",i), ".png"),width=graph_width,height=graph_width*dims[1]/dims[2])
    }
    
  }
  
  ###############
  
  doc <-  add_slide(doc, layout="Canreg_basic_wide", master="Office Theme") ## add PPTX slide (Title + content)
  doc <- ph_with_text(doc, type = "title", str = "Basis of diagnosis (DCO/Clinical/MV) by site")
  
  
  dt_basis <- csu_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL", "YEAR", "SEX", "BASIS"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL"))
  )
  
  dt_basis[BASIS > 0 & BASIS < 5, BASIS:=1]
  dt_basis[BASIS >= 5, BASIS:=2]
  
  dt_report <- canreg_ageSpecific_rate_data(dt_basis, keep_basis = TRUE)
  dt_report <- canreg_basis_table(dt_report)
  
  ft <- flextable(dt_report)
  ft <- set_header_labels(ft, CSU_label = "", CSU_ICD = "", total_cases= "",total_pc_test = "", 
                          BASIS_pc.0= "% DCO", BASIS_pc.1= "% Clinical", BASIS_pc.2= "% M.V") 
  ft <- add_header(ft, CSU_label = "Cancer site", CSU_ICD = "ICD-10", total_cases= "No. Cases",total_pc_test = "% total", 
                   BASIS_pc.0= "Basis of diagnosis", BASIS_pc.1= "Basis of diagnosis", BASIS_pc.2= "Basis of diagnosis",
                   top = TRUE ) 
  
  
  ft <- display( ft, col_key = "total_cases", pattern = "{{cases}}", 
                 formatters = list(cases ~ sprintf("%.00f", total_cases))) 
  
  ft <- width(ft,  width = 1.2)
  ft <- width(ft, j=1, width = 1.7)
  
  ft<- merge_h(ft,i=1, part="header")
  ft <- align(ft, align="center", part="header")
  ft <- border(ft ,i=1,j=1:4,border.bottom=fp_border(width = 0), part="header")
  ft <- height(ft, height = 0.331, part="header")
  ft <- height(ft, height = 0.22, part="body")
  ft <- bg(ft, i = seq(1,nrow(dt_report),2), bg="#deebf7", part = "body")
  ft <- bg(ft, i = nrow(dt_report), bg="#c6dbef", part = "body")
  ft <- bg(ft, i = 1, bg="#c6dbef", part = "header")
  ft <- bg(ft, i = 2, bg="#c6dbef", part = "header")
  
  doc <- ph_with_flextable_at(doc, ft, left=0.551, top=1.291)
  
  
  #################
  dt_report <- dt_all
  
  dt_report <- canreg_ageSpecific_rate_data(dt_report)
  
  canreg_output(output_type = "png", filename = paste0(tempdir(), "\\ann_temp_graph"),landscape = FALSE,
                list_graph = TRUE,
                FUN=canreg_ageSpecific_rate_multi_plot,dt=dt_report,group_by="SEX",var_age_label_list = "AGE_GROUP_LABEL",
                logscale = TRUE,  
                color_trend=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                multi_graph= FALSE,
                canreg_header=ls_args$header)
  
  dims <- attr( png::readPNG (paste0(tempdir(), "\\ann_temp_graph001.png")), "dim" )
  
  doc <-  add_slide(doc, layout="Canreg_info", master="Office Theme") ## add Canreg information slide.
  
  
  for (j in 1:length(levels(dt_report$ICD10GROUP))) {
    
    doc <-  add_slide(doc, layout="Canreg_vertical", master="Office Theme") ## add PPTX slide (Title + content)
    doc <- ph_with_text(doc, type = "title", str = paste0("Age-specific incidence rate:\r\n",  unique(dt_report[ICD10GROUP== levels(ICD10GROUP)[j] ,cancer_label])))
    doc <- ph_with_img(doc, paste0(tempdir(), "\\ann_temp_graph",sprintf("%03d",j) ,".png"), index=2, width=graph_width_vertical,height=graph_width_vertical*dims[1]/dims[2])
    
  }
  
  
  
  print(doc, target = ls_args$filename)
  
  

#talk to canreg
  cat(paste("-outFile",ls_args$filename,sep=":"))
	
	
  },
  
  error = function(e){
    if (exists("doc")) {
     writeDoc(doc, file = ls_args$filename)
     if (file.exists(ls_args$filename)) file.remove(ls_args$filename)
    }
    
    canreg_error_log(e,ls_args$filename,ls_args$out,Args,ls_args$inc,ls_args$pop)
  }
)
	
	
	
	
