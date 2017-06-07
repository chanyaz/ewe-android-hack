package com.expedia.bookings.hotel.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.algo.StaticCluster;
import com.google.maps.android.geometry.Bounds;

/**
 * Non-hierarchical distance based algorithm with a clause that says "if this item is selected, then it shouldn't be clustered
 */
public class HotelMapClusterAlgorithm extends NonHierarchicalDistanceBasedAlgorithm {

	@Override
	public Set<? extends Cluster<MapItem>> getClusters(double zoom) {

		final int discreteZoom = (int) zoom;

		final double zoomSpecificSpan = MAX_DISTANCE_AT_ZOOM / Math.pow(2, discreteZoom) / 256;

		final Set<QuadItem> visitedCandidates = new HashSet<QuadItem>();
		final Set<Cluster<MapItem>> results = new HashSet<Cluster<MapItem>>();
		final Map<QuadItem, Double> distanceToCluster = new HashMap<QuadItem, Double>();
		final Map<QuadItem, StaticCluster<MapItem>> itemToCluster = new HashMap<QuadItem, StaticCluster<MapItem>>();

		synchronized (mQuadTree) {
			for (QuadItem candidate : mItems) {
				if (visitedCandidates.contains(candidate)) {
					// Candidate is already part of another cluster.
					continue;
				}

				// We mark and set aside the selected item.
				if (candidate.mClusterItem.isSelected()) {
					results.add(candidate);
					visitedCandidates.add(candidate);
					distanceToCluster.put(candidate, -1d);
					continue;
				}

				Bounds searchBounds = createBoundsFromSpan(candidate.getPoint(), zoomSpecificSpan);
				Collection<QuadItem> clusterItems;
				clusterItems = mQuadTree.search(searchBounds);
				if (clusterItems.size() == 1) {
					// Only the current marker is in range. Just add the single item to the results.
					results.add(candidate);
					visitedCandidates.add(candidate);
					distanceToCluster.put(candidate, 0d);
					continue;
				}
				StaticCluster<MapItem> cluster = new StaticCluster<MapItem>(candidate.mClusterItem.getPosition());
				results.add(cluster);

				for (QuadItem clusterItem : clusterItems) {
					if (clusterItem.mClusterItem.isSelected()) {
						if (visitedCandidates.contains(clusterItem)) {
							continue;
						}
						// We mark and set aside the selected item.
						results.add(clusterItem);
						visitedCandidates.add(clusterItem);
						distanceToCluster.put(clusterItem, -1d);
						continue;
					}
					Double existingDistance = distanceToCluster.get(clusterItem);
					double distance = distanceSquared(clusterItem.getPoint(), candidate.getPoint());
					if (existingDistance != null) {
						// Item already belongs to another cluster. Check if it's closer to this cluster.
						if (existingDistance < distance) {
							continue;
						}
						// Move item to the closer cluster.
						itemToCluster.get(clusterItem).remove(clusterItem.mClusterItem);
					}
					distanceToCluster.put(clusterItem, distance);
					cluster.add(clusterItem.mClusterItem);
					itemToCluster.put(clusterItem, cluster);
				}
				visitedCandidates.addAll(clusterItems);
			}
		}
		return results;
	}

}
