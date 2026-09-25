import type { Notification } from '../entity/Notification'

type ActivityFeedProps = {
  entries: Notification[]
}

function labelFor(message: string) {
  const normalized = message.toLowerCase()
  if (normalized.includes('reject')) return 'Rejected'
  if (normalized.includes('cancel')) return 'Cancelled'
  if (normalized.includes('deliver')) return 'Delivered'
  if (normalized.includes('reorder')) return 'Reorder'
  if (normalized.includes('low stock')) return 'Low stock'
  return 'Update'
}

function clock(iso: string) {
  return new Date(iso).toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

export function ActivityFeed({ entries }: ActivityFeedProps) {
  return (
    <section className="mt-12" aria-labelledby="activity-feed-title">
      <div className="flex items-end justify-between border-b border-stone-100/20 pb-3">
        <h2 id="activity-feed-title" className="font-mono text-xs font-bold uppercase tracking-[0.12em] text-lime-200">
          Activity
        </h2>
        {entries.length > 0 && <span className="font-mono text-xs text-stone-100/60">{entries.length} updates</span>}
      </div>
      {entries.length === 0 ? (
        <p className="py-5 text-sm text-stone-100/60">Order and reorder updates will appear here.</p>
      ) : (
        <ol className="divide-y divide-stone-100/15">
          {entries.map((entry) => (
            <li className="py-4" key={entry.notificationId}>
              <div className="flex items-center justify-between gap-3 font-mono text-xs">
                <span className="font-bold uppercase tracking-[0.08em] text-lime-200">{labelFor(entry.message)}</span>
                <time dateTime={entry.createdAt} className="text-stone-100/50">{clock(entry.createdAt)}</time>
              </div>
              <p className="mt-2 text-sm text-stone-100/80">{entry.message}</p>
            </li>
          ))}
        </ol>
      )}
    </section>
  )
}
