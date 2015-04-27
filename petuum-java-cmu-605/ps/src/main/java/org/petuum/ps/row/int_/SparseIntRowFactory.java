package org.petuum.ps.row.int_;

import org.petuum.ps.config.Config;
import org.petuum.ps.row.Row;
import org.petuum.ps.row.RowFactory;

/**
 * Created by aqiao on 3/5/15.
 */
public class SparseIntRowFactory implements RowFactory {

    @Override
    public Row createRow(Config rowConfig) {
        return new SparseIntRow();
    }
}
