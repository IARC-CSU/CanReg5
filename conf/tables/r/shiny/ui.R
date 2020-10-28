## version : 1.0


library(shinydashboard)
library(shinyjs)


ui <- dashboardPage(

  title="Canreg5 interactive graph",
  dashboardHeader(	title = HTML(
      "<div style = 'vertical-align:middle'>
       <img src = 'LogoBetaNew64x64.ico' class='canreg_logo' align = 'center'>
       </div>"),
       titleWidth = 350),
  
	

	
  dashboardSidebar(
    
  	useShinyjs(),
    tags$link(rel = "stylesheet", type = "text/css", href = "registry_graph.css"),
    width = 350,
    
    tags$div(class="subHeader", checked=NA,
             tags$p("Graphics")
    ),
    		
		uiOutput("UI_select_table"),
		
		tags$div(id="export_menu",
			tags$div(class="subHeader", checked=NA,
			         tags$p("Export Graph")
			),
			selectInput("select_format", "Format", as.list(c( "pdf","tiff","png", "svg", "ps","csv"))),
			
			textInput("text_filename", "Filename", "CanReg5_graph"),
			
			downloadButton('downloadFile', 'Export graph', class="mat_btn"),
			
			tags$div(class="subHeader", checked=NA,
			         tags$p("Powerpoint presentation")
			),
			
			actionButton('actionSlide', "Add to presentation",  class="mat_btn"),
			
			textInput("pptx_filename", "Powerpoint filename", "CanReg5_slide"),
			
			downloadButton('downloadPres', 'Create presentation',  class="mat_btn"),
			
			uiOutput("UI_nbSlide")

		),

		tags$div(id="div_advanced", class="mt20",
			checkboxInput("CheckAdvanced", "Advanced shiny option",FALSE)
		),

		tags$div(id="advanced_menu",style="display: none;",

			tags$div(class="subHeader", checked=NA,
	             tags$p("Shiny data sytem")
		    ),

		    fileInput("shinydata", "Import shiny data",
		          multiple = FALSE,
		          accept = c(".zip")
		          ),

		    downloadButton('downloadShinyData', 'Export shiny data', class="mat_btn mt15m")
		 )

	 ),
	  
	  						
	dashboardBody(
		tags$style(type='text/css',
                   ".selectize-dropdown-content{
                 		max-height: 300px;
                	}"
        ),
		tags$script(HTML(paste0('$(document).ready(function(){
											$("header").find("nav").append(\'<div class="regtitle">',textOutput("UI_regtitle"),'</div>\');
                    })'
   		))),
	  useShinyjs(),
		downloadButton('downloadLog', '', class="log"),
		downloadButton('downloadData', '', class="log"),
		fluidRow(id="fluid_test",
			column(4,uiOutput("UI_control5")),
		 	column(4,uiOutput("UI_control6")),
	    column(4,uiOutput("UI_control7"))
	    	
	      ),
	  fluidRow(
	      plotOutput("plot", height ="600px")
	   ),
	  fluidRow(id="report_option",style="display: none;",
	  	
			column(6,
				tags$h3("DOCX report"),
				tags$p("Several chapters in the report are generated based on a list of template files."),
				tags$p(tags$b("Please, select a folder to store the report and the template files:")),
				textOutput("directorypath"),
				tags$br(),
				htmlOutput("reportHTML"),
				downloadButton('downloadReport', 'Create DOCX report', class="report_btn")),
			column(6,
				tags$h3("PPTX report"),
				downloadButton('downloadSlide', 'Create PPTX report', class="report_btn"))
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