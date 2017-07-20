package com.sk_scd91.basicqrscanner;

/**
 *
 * (c) 2017 Sean Deneen
 *
 */

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;
import com.sk_scd91.basicqrscanner.db.QRCodeSQLHelper;
import com.sk_scd91.basicqrscanner.db.QRDB;
import com.sk_scd91.basicqrscanner.db.QRDBLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * A Fragment that displays a list of barcodes from the database.
 */
public class BarcodeListActivityFragment extends Fragment {
    
    private static final int LOADER_ID = 1;

    private BarcodeListAdapter mBarcodeAdapter;
    private RecyclerView mRecyclerView;

    public BarcodeListActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBarcodeAdapter = new BarcodeListAdapter();
            getLoaderManager().initLoader(LOADER_ID, null,
                    new LoaderManager.LoaderCallbacks<List<Barcode>>() {
                @Override
                public Loader<List<Barcode>> onCreateLoader(int id, Bundle args) {
                    return new QRDBLoader(getContext());
                }

                @Override
                public void onLoadFinished(Loader<List<Barcode>> loader, List<Barcode> data) {
                    mBarcodeAdapter.addBarcodes(data);
                }

                @Override
                public void onLoaderReset(Loader<List<Barcode>> loader) {

                }
            });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_barcode_list, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.content_barcode_recycler);
        mRecyclerView.setAdapter(mBarcodeAdapter);

        ItemTouchHelper swipeToDeleteHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    removeBarcodeFromList(viewHolder.itemView, viewHolder.getAdapterPosition());
                }
            }
        });
        swipeToDeleteHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                RecyclerView.VERTICAL));

        return v;
    }

    private void removeBarcodeFromList(View itemView, final int position) {
        final Barcode removed = mBarcodeAdapter.removeBarcode(position);
        String removeText = String.format("Removed %s", removed.displayValue);
        Snackbar.make(itemView, removeText, Snackbar.LENGTH_SHORT)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBarcodeAdapter.insertBarcode(position, removed);
                    }
                }).addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar snackBar, int event) {
                if (event != BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION) {
                    QRCodeSQLHelper sqlHelper = new QRCodeSQLHelper(snackBar.getContext());
                    try {
                        QRDB.deleteFromDB(sqlHelper.getWritableDatabase(), removed);
                    } finally {
                        sqlHelper.close();
                    }
                }
            }
        }).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Allow RecyclerView to be garbage collected before fragment is.
        mRecyclerView.setAdapter(null);
        mRecyclerView = null;
    }

    /**
     * Insert a barcode into the list, then save into the database.
     * @param barcode The barcode to insert into the list and database.
     */
    public void setNewBarcode(Barcode barcode) {
        mBarcodeAdapter.insertBarcode(0, barcode);
        QRCodeSQLHelper sqlHelper = new QRCodeSQLHelper(getContext().getApplicationContext());
        try {
            QRDB.insertToDB(sqlHelper.getWritableDatabase(), barcode);
        } finally {
            sqlHelper.close();
        }
    }

    private static class BarcodeListAdapter extends RecyclerView.Adapter<BarcodeListAdapter.ViewHolder> {
        private ArrayList<Barcode> barcodes = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_barcode_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.setViewForBarcode(barcodes.get(position));
        }

        @Override
        public int getItemCount() {
            return barcodes.size();
        }

        public void addBarcodes(List<Barcode> newBarcodes) {
            if (newBarcodes != null && !newBarcodes.isEmpty()) {
                barcodes.clear();
                barcodes.ensureCapacity(newBarcodes.size());
                barcodes.addAll(newBarcodes);
            } else {
                barcodes.clear();
            }
            notifyDataSetChanged();
        }

        public void insertBarcode(int position, Barcode barcode) {
            for (int i = barcodes.size() - 1; i >= 0; i--) {
                if (barcode.rawValue.equals(barcodes.get(i).rawValue)) {
                    moveBarcodes(i, position);
                    return;
                }
            }

            barcodes.add(position, barcode);
            notifyItemInserted(position);
        }

        public Barcode removeBarcode(int index) {
            Barcode removed = barcodes.remove(index);
            notifyItemRemoved(index);
            return removed;
        }

        public void moveBarcodes(int from, int to) {
            if (from == to) // Nothing to do if moving to the same position.
                return;
            Barcode removed = barcodes.remove(from);
            barcodes.add(to, removed);
            notifyItemMoved(from, to);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ViewHolder(View itemView) {
                super(itemView);
            }

            public void setViewForBarcode(Barcode barcode) {
                TextView itemTypeView = (TextView) itemView.findViewById(R.id.barcode_item_type);
                TextView itemTextView = (TextView) itemView.findViewById(R.id.barcode_item_text);

                Resources resources = itemView.getResources();
                itemTypeView.setText(resources.getString(R.string.format_info_qr_type,
                        resources.getString(Utils.getNameOfBarcodeType(barcode.valueFormat))));
                Utils.setAutoLinkForBarcodeType(itemTextView, barcode.valueFormat);
                itemTextView.setText(barcode.displayValue);
            }
        }
    }

}
