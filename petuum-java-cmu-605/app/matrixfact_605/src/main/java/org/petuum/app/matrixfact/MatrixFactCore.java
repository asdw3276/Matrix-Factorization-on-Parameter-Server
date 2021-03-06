package org.petuum.app.matrixfact;

import org.petuum.app.matrixfact.Rating;
import org.petuum.app.matrixfact.LossRecorder;

import org.petuum.ps.PsTableGroup;
import org.petuum.ps.row.double_.DenseDoubleRow;
import org.petuum.ps.row.double_.DenseDoubleRowUpdate;
import org.petuum.ps.row.double_.DoubleRow;
import org.petuum.ps.row.double_.DoubleRowUpdate;
import org.petuum.ps.table.DoubleTable;
import org.petuum.ps.common.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class MatrixFactCore {
      private static final Logger logger =
                    LoggerFactory.getLogger(MatrixFactCore.class);

        public static void sgdOneRating(Rating r, double learningRate,
                      DoubleTable LTable, DoubleTable RTable, int K, double lambda)
              {
                      int i = r.userId;
                      int j = r.prodId;

                      DoubleRow rowCache = new DenseDoubleRow(K+1);
                      DoubleRow lrow = LTable.get(i);
                      rowCache.reset(lrow);
                      double[] Li = new double[K];
                      for(int p = 0; p < K; p++)
                      {
                         Li[p] = rowCache.getUnlocked(p);
                      }
                      double  ni = rowCache.getUnlocked(K);
                      rowCache = new DenseDoubleRow(K+1);
                      DoubleRow rRow = RTable.get(j);
                      rowCache.reset(rRow);
                      double[] Rj = new double[K];
                      for(int p = 0; p < K; p++)
                      {
                          Rj[p] = rowCache.getUnlocked(p);
                      }
                      double mj = rowCache.getUnlocked(K);
                      double[] Lchange = new double[K];
                      double[] Rchange = new double[K];
                      double eij = 0;
                      for(int p = 0; p < K; p++)
                      {
                          eij += Li[p] * Rj[p];
                      }
                      eij = r.rating - eij;
                      for(int p = 0; p < K; p++)
                      {
                          Lchange[p] = (eij * Rj[p] - Li[p] * lambda / ni) * 2 * learningRate;
                          Rchange[p] = (eij * Li[p] - Rj[p] * lambda / mj) * 2 * learningRate;
                      }
                      DoubleRowUpdate Lupdates = new DenseDoubleRowUpdate(K);
                      for(int p = 0; p < K; p++)
                          Lupdates.setUpdate(p, Lchange[p]);
                      LTable.batchInc(i, Lupdates);
                      DoubleRowUpdate Rupdates = new DenseDoubleRowUpdate(K);
                      for(int p = 0; p < K; p++)
                          Rupdates.setUpdate(p, Rchange[p]);
                      RTable.batchInc(j, Rupdates);
              }
          public static void evaluateLoss(ArrayList<Rating> ratings,
                        int ithEval, int elemBegin, int elemEnd, DoubleTable
                              LTable, DoubleTable RTable, int LRowBegin, int LRowEnd,
                                    int RRowBegin, int RRowEnd, LossRecorder lossRecorder, int
                                          K, double lambda)
                {
                            double sqLoss = 0;
                            double totalLoss = 0;
                            double LL2norm = 0;
                            for(int q = LRowBegin; q < LRowEnd; q++)
                            {
                                DoubleRow Lrow = LTable.get(q);
                                for(int p = 0; p < K; p++)
                                {
                                    LL2norm += Math.pow(Lrow.get(p),2);
                                }
                            }
                            double RL2norm = 0;
                            for(int q = RRowBegin; q < RRowEnd; q++)
                            {
                                DoubleRow Rrow = RTable.get(q);
                                for(int p = 0; p < K; p++)
                                {
                                    RL2norm += Math.pow(Rrow.get(p),2);
                                }
                            }
                            for(int p = elemBegin; p < elemEnd; p++)
                            {
                                Rating r = ratings.get(p);
                                int i = r.userId;
                                int j = r.prodId;
                                DoubleRow lrow = LTable.get(i);
                                double[] Li = new double[K];
                                for(int s = 0; s < K; s++)
                                {
                                    Li[s] = lrow.get(s);
                                }
                                DoubleRow rRow = RTable.get(j);
                                double[] Rj = new double[K];
                                for(int s = 0; s < K; s++)
                                {
                                    Rj[s] = rRow.get(s);
                                }
                                double loss = 0;
                                for(int q = 0; q < K; q++)
                                {
                                    loss += Li[q] * Rj[q];
                                }
                                loss = loss - r.rating;
                                loss = Math.pow(loss,2);
                                sqLoss += loss;
                            }
                            totalLoss = sqLoss + lambda * (LL2norm + RL2norm);
                            lossRecorder.incLoss(ithEval, "SquareLoss", sqLoss);
                            lossRecorder.incLoss(ithEval, "FullLoss", totalLoss);
                            lossRecorder.incLoss(ithEval, "NumSamples", elemEnd - elemBegin);
                }
}

