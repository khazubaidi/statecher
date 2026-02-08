//package io.github.khazubaidi;
//
//import io.github.khazubaidi.autoconfigure.StatechersLoader;
//import io.github.khazubaidi.bootstrapers.StatechersRegistry;
//import io.github.khazubaidi.autoconfigure.BeanReferenceValidator;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class StatechersConfiguration {
//
//    private static final Logger log = LoggerFactory.getLogger(StatechersConfiguration.class);
//
//    private final StatechersLoader statechersLoader;
//    private final BeanReferenceValidator beanReferenceValidator;
//    private final StatechersRegistry statechersRegistry;
//
//    public StatechersConfiguration(StatechersLoader statechersLoader,
//                                  BeanReferenceValidator beanReferenceValidator,
//                                  StatechersRegistry statechersRegistry) {
//
//        this.statechersLoader = statechersLoader;
//        this.beanReferenceValidator = beanReferenceValidator;
//        this.statechersRegistry = statechersRegistry;
//
//        log.info("Statechers auto-configuration initialized");
//    }
//}
