module ConcreteCubesValidator {
    requires javafx.fxml;
    requires transitive javafx.controls;
	requires java.prefs;
	requires org.slf4j;
	requires ch.qos.logback.classic;
	//required becouse of bug in logback
	requires java.naming;
	
    opens mtw.concretecubesvalidator.view to javafx.fxml;
    
    exports mtw.concretecubesvalidator;
    exports mtw.concretecubesvalidator.model;
    exports mtw.concretecubesvalidator.model.communication;
}