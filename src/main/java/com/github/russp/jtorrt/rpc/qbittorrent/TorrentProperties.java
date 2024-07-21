package com.github.russp.jtorrt.rpc.qbittorrent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A record representing detailed information about a torrent.
 *
 * @param save_path                The path where the torrent is saved.
 * @param creation_date            The creation date of the torrent in Unix timestamp format.
 * @param piece_size               The size of each piece of the torrent in bytes.
 * @param comment                  A comment associated with the torrent.
 * @param total_wasted             The total amount of data wasted for the torrent in bytes.
 * @param total_uploaded           The total amount of data uploaded for the torrent in bytes.
 * @param total_uploaded_session   The total amount of data uploaded this session in bytes.
 * @param total_downloaded         The total amount of data downloaded for the torrent in bytes.
 * @param total_downloaded_session The total amount of data downloaded this session in bytes.
 * @param up_limit                 The upload limit for the torrent in bytes per second.
 * @param dl_limit                 The download limit for the torrent in bytes per second.
 * @param time_elapsed             The elapsed time of the torrent in seconds.
 * @param seeding_time             The elapsed time while the torrent is complete in seconds.
 * @param nb_connections           The number of connections for the torrent.
 * @param nb_connections_limit     The connection count limit for the torrent.
 * @param share_ratio              The share ratio of the torrent.
 * @param addition_date            The date when the torrent was added in Unix timestamp format.
 * @param completion_date          The completion date of the torrent in Unix timestamp format.
 * @param created_by               The creator of the torrent.
 * @param dl_speed_avg             The average download speed of the torrent in bytes per second.
 * @param dl_speed                 The current download speed of the torrent in bytes per second.
 * @param eta                      The estimated time of arrival for the torrent in seconds.
 * @param last_seen                The last seen complete date of the torrent in Unix timestamp format.
 * @param peers                    The number of peers connected to the torrent.
 * @param peers_total              The total number of peers in the swarm.
 * @param pieces_have              The number of pieces owned by the torrent.
 * @param pieces_num               The total number of pieces of the torrent.
 * @param reannounce               The number of seconds until the next announce.
 * @param seeds                    The number of seeds connected to the torrent.
 * @param seeds_total              The total number of seeds in the swarm.
 * @param total_size               The total size of the torrent in bytes.
 * @param up_speed_avg             The average upload speed of the torrent in bytes per second.
 * @param up_speed                 The current upload speed of the torrent in bytes per second.
 * @param isPrivate                True if the torrent is from a private tracker.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TorrentProperties(
		String save_path,
		int creation_date,
		int piece_size,
		String comment,
		long total_wasted,
		long total_uploaded,
		long total_uploaded_session,
		long total_downloaded,
		long total_downloaded_session,
		long up_limit,
		long dl_limit,
		int time_elapsed,
		int seeding_time,
		int nb_connections,
		int nb_connections_limit,
		float share_ratio,
		int addition_date,
		int completion_date,
		String created_by,
		long dl_speed_avg,
		long dl_speed,
		int eta,
		int last_seen,
		int peers,
		int peers_total,
		int pieces_have,
		int pieces_num,
		int reannounce,
		int seeds,
		long seeds_total,
		long total_size,
		long up_speed_avg,
		long up_speed,
		boolean isPrivate
) {
}
