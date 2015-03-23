package net.gcdc.geonetworking;

public interface LocationTableListener
{

  /** Notify a property change for given entry.
   *
   * @param entry The entry changed, if <code>null</code> indicates the change applies to multiple entries.
   *
   */
  public void notifyEntryChanged (LocationTable.Entry entry);

  /** Notify a structural change (addition/removal of one or more entries) in the {@link LocationTable}.
   *
   */
  public void notifyStructureChanged ();

}
