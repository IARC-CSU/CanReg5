
library(shinydashboard)
library(shinyjs)

ui <- dashboardPage(

  title="Canreg5 table",
  dashboardHeader(title = "CanReg5",
                  titleWidth = 350),
  
  dashboardSidebar(
    
    tags$link(rel = "stylesheet", type = "text/css", href = "registry_graph.css"),
    width = 350,
    
    tags$div(class="subHeader", checked=NA,
             tags$p("Graph Selection")
    ),
    		
		selectInput("select_table", "Graphics", 
		            c( "Population pyramid" = 1,
					   "Barchart of cases by age group by sex" = 2, 
					   "piechart of cases by age group by sex" = 3, 
					   "Top cancer both sexes" = 4 ,
		         "Top cancer by sexes" = 5 ,
		               "Age-specific rates (Top Cancer Sites)" = 6,
		               "Age-specific rate by cancer sites" = 7,
					   "Top cancer, sex specific" = 8,
					   "CI5 comparison" = 8,
					   "Time trends (Top cancer Sites)" = 9,
					   "EAPC scatter graph = 10"
		              )
		              
		            ),
		
		tags$div(class="subHeader", checked=NA,
		         tags$p("Export Graph")
		),
		
		selectInput("select_format", "Format", as.list(c( "pdf","tiff","png", "svg", "ps","csv"))),
		
		textInput("text_filename", "Filename", "CR5_graph"),
		
		downloadButton('downloadFile', 'Export graph', class="mat_btn"),
		
		tags$div(class="subHeader", checked=NA,
		         tags$p("Powerpoint presentation")
		),
		
		actionButton('actionSlide', "Add to presentation",  class="mat_btn"),
		
		textInput("pptx_filename", "Powerpoint filename", "CR5_slide"),
		
		downloadButton('downloadPres', 'Create presentation',  class="mat_btn"),
		
		uiOutput("UI_nbSlide")
  	
	   
	 ),
	  
	  
	dashboardBody(
	
		tags$script(HTML(paste0('$(document).ready(function(){
											$("header").find("nav").append(\'<div class="regtitle">',ls_args$header,'</div>\');
                    })
   '))),
	  useShinyjs(),
	 
	  fluidRow(

	      plotOutput("plot", height ="600px")

	   ),
	  fluidRow(
	    box(id="controls_COL1",
	      uiOutput("UI_control1"),
	      uiOutput("UI_control2")
	    ),
	    box(id="controls_COL2",
  	    uiOutput("UI_control3"),
  	    uiOutput("UI_control4")
	    )
	  )
	)
)