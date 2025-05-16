package eu.openanalytics.phaedra.measservice.record;

import java.util.Date;
import java.util.List;

public record FilterOptions(List<Long> ids, Date from, Date to) {
}
